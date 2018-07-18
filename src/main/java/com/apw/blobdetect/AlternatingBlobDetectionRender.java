package com.apw.blobdetect;

import com.apw.blobfilter.BlobFilter;
import com.apw.blobfilter.IMovingBlobReduction;
import com.apw.blobtrack.IMovingBlobDetection;
import com.apw.blobtrack.MovingBlob;
import com.apw.blobtrack.MovingBlobDetection;
import com.apw.fly2cam.IAutoExposure;
import com.apw.fly2cam.ToggleExposure;
import com.apw.oldimage.IImage;
import com.apw.oldimage.IPixel;
import com.apw.oldimage.Image;
import com.apw.oldimage.Pixel;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import java.util.List;

public class AlternatingBlobDetectionRender extends Application {
    boolean drawBlobs = true; // boolean for whether or not we draw blobs in the render class
    boolean filter = true; // enables or disables the blob filter
    boolean posterize = false; // enables or disables posterization
    private int offset = 0;

    public static void main(String... args) {
        launch(args);
    }

    private static Paint getPaint(IPixel p) {
        switch (p.getColor()) {
            case 0:
                return (Color.RED);
            case 1:
                return (Color.LIME);//color set to lime, because it is fully saturated (unlike "green")
            case 2:
                return (Color.BLUE);
            case 3:
                return (Color.GRAY);
            case 4:
                return (Color.BLACK);
            case 5:
                return (Color.WHITE);
            default:
                throw new IllegalStateException("Invalid color code " + p.getColor() + ".");
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
//         IImage -image = new JpgImage("src/testImage1.png");
//        IImage image = new Image(0, 50, 0);
        IImage image = new Image();

        IAutoExposure autoExposure = new ToggleExposure(image);
        IBlobDetection blobDetect = new BlobDetection();
        IMovingBlobDetection movingBlobDetect = new MovingBlobDetection();
        IMovingBlobReduction blobFilter = new BlobFilter();

        IPixel[][] pixels = image.getImage();
        final int scale = 1;

        if (pixels.length == 0) {
            System.err.println("Please plug in the camera.");
            System.exit(1);
        }

        final int width = pixels[0].length;
        final int height = pixels.length;

        Canvas canvas = new Canvas(2 * width * scale, height * scale);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        image.setAutoFreq(15);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long time) {
                image.readCam();
                IPixel[][] pixels = image.getImage();

                final int width = pixels[0].length;
                final int height = pixels.length;

                final float blockedOutArea = (0);
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        if (j < (height * blockedOutArea)) {
                            gc.setFill(Color.RED);
                            pixels[j][i] = new Pixel((short) 255, (short) 0, (short) 0);
                        } else {
                            //@formatter:off

                            IPixel p = pixels[j][i];
                            Paint fill = Color.rgb(p.getRed(), p.getGreen(), p.getBlue());

                            if (posterize) {
                                fill = getPaint(p);
                            }

                            gc.setFill(fill);

                            //@formatter:on
                        }

                        gc.fillRect(i * scale + offset, j * scale, scale, scale);
                    }
                }

                autoExposure.autoAdjust(image.getImage());

                List<Blob> blobs = blobDetect.getBlobs(image);
                List<MovingBlob> movingBlobs = movingBlobDetect.getMovingBlobs(blobs);

                List<MovingBlob> filteredBlobs = blobFilter
                        .filterMovingBlobs(movingBlobDetect.getUnifiedBlobs(blobFilter.filterMovingBlobs(movingBlobs)));

                gc.setStroke(Color.DARKGOLDENROD);
                gc.setLineWidth(4);

                if (drawBlobs) {
                    if (filter) {
                        for (Blob blob : filteredBlobs) {
                            gc.strokeRect(blob.x * scale, blob.y * scale, blob.width * scale, blob.height * scale);
                        }
                    } else {
                        for (Blob blob : blobs) {
                            gc.strokeRect(blob.x * scale, blob.y * scale, blob.width * scale, blob.height * scale);
                        }
                    }
                }

                offset = (offset + width) % (2 * width);
            }
        };

        timer.start();

        primaryStage.setTitle("JavaFX Window");

        Group rootNode = new Group();
        rootNode.getChildren().addAll(canvas);

        Scene myScene = new Scene(rootNode);
        primaryStage.setScene(myScene);

        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent arg0) {
                switch (arg0.getCode()) {
                    case P:
                        posterize = !posterize;
                        break;
                    case B:
                        drawBlobs = !drawBlobs;
                        break;
                    case F:
                        filter = !filter;
                        break;
                    case ESCAPE:
                        image.finish();
                        System.out.println("image finished");
                        System.exit(0);
                        break;
                    default:
                        break;
                }
            }
        });

        primaryStage.setOnCloseRequest(event ->
        {
            image.finish();
            System.out.println("image finished");
        });

        primaryStage.show();
    }
}
