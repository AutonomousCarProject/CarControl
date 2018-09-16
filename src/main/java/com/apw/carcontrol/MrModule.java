package com.apw.carcontrol;

import com.apw.imagemanagement.ImageManagementModule;
import com.apw.sbcio.PWMController;
import com.apw.sbcio.fakefirm.ArduinoIO;
import com.apw.sbcio.fakefirm.ArduinoModule;
import com.apw.speedcon.SpeedControlModule;

import com.apw.steering.SteeringModule;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import org.jetbrains.annotations.NotNull;

public class MrModule extends JFrame implements Runnable, KeyListener {

    private ExecutorService steeringExec = Executors.newSingleThreadExecutor(new NamedThreadFactory("steering"));
    private ExecutorService speedExec = Executors.newSingleThreadExecutor(new NamedThreadFactory("speed"));
    private ExecutorService imageRGBExec = Executors.newSingleThreadExecutor(new NamedThreadFactory("imageRGB"));
    private ExecutorService imageBWExec = Executors.newSingleThreadExecutor(new NamedThreadFactory("imageBW"));
    private ExecutorService imageSimpleExec = Executors.newSingleThreadExecutor(new NamedThreadFactory("imageSimple"));
    private ExecutorService cameraImageExec = Executors.newSingleThreadExecutor(new NamedThreadFactory("cameraReader"));
    private PWMController driveSys = new ArduinoIO();
    private List<Module> modules = new ArrayList<>(); // Contains each module
    private final WindowModule windowModule;
    private final ArduinoModule arduinoModule;
    private final ImageManagementModule imageManagementModule;
    private final SpeedControlModule speedControlModule;
    private final SteeringModule steeringModule;
    private final CarControl carControl; // A CarControl that holds data for each module
    //private CarControl speedControl; // A CarControl that holds data specifically for speed
    private CarControl steeringControl; // A CarControl that holds data specifically for steering
    private long frameNumber = 0L;
    private long lastTime = 0L;

    private boolean initialized = false;

    private static final int FPS = 50; // Number of frames per second run is called
    private static final int initDelay = 100; // Initial delay before run is called

    private MrModule(boolean realCam, boolean hasWindow) {
        if (realCam) {
            carControl = new CamControl(driveSys);
            //speedControl = new CamControl(driveSys);
            steeringControl = new CamControl(driveSys);
        } else {
            carControl = new TrakSimControl(driveSys);
            //speedControl = new TrakSimControl(driveSys);
            steeringControl = new TrakSimControl(driveSys);
        }

        final int winWidth = carControl.getImageWidth();
        final int winHeight = carControl.getImageHeight();
        carControl.updateWindowDims(getWidth(), getHeight());

        // Create modules
        windowModule = createWindowModule(hasWindow, winWidth, winHeight);
        arduinoModule = new ArduinoModule(driveSys);
        imageManagementModule = new ImageManagementModule(winWidth, winHeight, carControl.getTile());
        speedControlModule = new SpeedControlModule();
        steeringModule = new SteeringModule();

        initializeModules(windowModule, arduinoModule, imageManagementModule, speedControlModule, steeringModule);

        headlessInit();
    }

    /**
     * Gets called once, and starts the executor service to call run every FPS
     */
    private void headlessInit() {
        // driveSys = new ArduinoIO();
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Frame Scheduler"));
        executorService.scheduleAtFixedRate(this, initDelay, Math.round(1000.0 / FPS), TimeUnit.MILLISECONDS);

        Future run = executorService.submit(this);

        try {
            run.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void initializeModules(Module... moduleArray) {
        modules = Arrays.asList(moduleArray);

        for (Module module : modules) {
            module.initialize(steeringControl);
        }

        initialized = true;
    }

    private WindowModule createWindowModule(boolean useWindowModule, int winWidth, int winHeight) {
    	final WindowModule winModule;
    	if (useWindowModule) {
	        winModule = new WindowModule(winWidth, winHeight);
	        winModule.addKeyListener(this);
    	} else {
    	    winModule = null;
        }
        return winModule;
    }

    /**
     * <p>
     * Update method is called once every FPS.
     * Method calls and manages each module's update. Specifically, ImageManagement, Speed and steering.
     * ImageManagement, speed and steering are each in their own thread. Speed and steering
     * run after ImageManagement process their respective image.
     * </p>
     */
    private void update(long frameNumber) {
        //read the camera image, and update windowModule.
        CompletableFuture<Void> cameraImage = CompletableFuture
                .runAsync(() -> {
                            carControl.readCameraImage();
                            carControl.setEdges(getInsets());
                            windowModule.update(carControl);
                            arduinoModule.update(carControl);
                        }, cameraImageExec);

        // FIXME: Does not print exceptions to console.

        final byte[] recentImage = carControl.getRecentCameraImage();

        // Start Thread to get the black and white image (for steering)
        CompletableFuture<Void> futureSteering = cameraImage
                .thenApplyAsync(v -> setBWImage(recentImage), imageBWExec)
                // Call steering Module after futureBWImage is finished
                .thenAcceptAsync(steeringModule::update, steeringExec);

        CompletableFuture<CarControl> futureRGBImage = CompletableFuture.completedFuture(null);
        if (frameNumber % 1 == 0) {
            futureRGBImage = cameraImage
                    .thenApplyAsync(v -> setRGBImage(recentImage), imageRGBExec);
        }

        // Call speed module after futureSimpleImage is finished
        CompletableFuture<Void> futureSpeed = CompletableFuture.completedFuture(null);
        if (frameNumber % 3 == 1) {
            // Start Thread to get the simple image (for speed)
            futureSpeed = cameraImage
                    .thenApplyAsync(v -> setSimpleImage(recentImage), imageSimpleExec)
                    .thenAcceptAsync(speedControlModule::update, speedExec);
        }

        // Run when all finished.
        CompletableFuture.allOf(futureSpeed, futureSteering, futureRGBImage)
                .thenAccept(v -> paint())
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                })
                .join();

    }

    private CarControl setRGBImage(byte[] recentImage) {
        carControl.setRGBImage(imageManagementModule.getRGBRaster(recentImage));
        carControl.setRenderedImage(carControl.getRGBImage());
        return carControl;
    }

    private CarControl setBWImage(byte[] recentImage) {
        steeringControl.setRGBImage(imageManagementModule.getBlackWhiteRaster(recentImage));
        return steeringControl;
    }

    private CarControl setSimpleImage(byte[] recentImage) {
        steeringControl.setProcessedImage(imageManagementModule.getSimpleColorRaster(recentImage));
        return steeringControl;
    }

    private void paint() {
        if (!modules.isEmpty()) {
            for (Module module : modules) {
                module.paint(carControl, ((WindowModule) (modules.get(0))).getGraphics());
            }
        }
    }

    @Override
    public void run() {
        if (initialized) {
            //printElapsedTime();
            try {
                update(++frameNumber);
                //paint();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void printElapsedTime() {
        final long curTime = System.currentTimeMillis();
        if (lastTime == 0) {
            lastTime = curTime;
        }
        System.out.println("Time: " + (lastTime - curTime));
        lastTime = curTime;
    }

    public static void main(String[] args) {
        boolean realcam = true;
        boolean window = true;
        if(args.length > 0) {
        	if(args[0].toLowerCase().equals("sim")) {
        		realcam = false;
        	}
        	if(args[0].toLowerCase().equals("headless")) {
        		window = false;
        	}
        }
        new MrModule(realcam, window);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (carControl instanceof TrakSimControl) {
            for (Map.Entry<Integer, Runnable> binding : ((TrakSimControl) carControl).keyBindings.entrySet())
                if (e.getKeyCode() == binding.getKey())
                    binding.getValue().run();
        } else if (carControl instanceof CamControl) {
        	for (Map.Entry<Integer, Runnable> binding : ((CamControl) carControl).keyBindings.entrySet())
                if (e.getKeyCode() == binding.getKey())
                    binding.getValue().run();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { }

    @Override
    public void keyTyped(KeyEvent e) { }

    public static class NamedThreadFactory implements ThreadFactory {
        private final String name;

        public NamedThreadFactory(String name) {
            this.name = name;
        }

        @Override
        public Thread newThread(@NotNull Runnable r) {
            return new Thread(r, name);
        }
    }
}
