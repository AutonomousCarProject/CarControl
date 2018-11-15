package com.apw.steering;

import com.aparapi.internal.tool.InstructionHelper;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.JFrame;
import org.jetbrains.annotations.NotNull;

public class DataCollection extends JFrame {

    private Scanner fileScanner;
    private BufferedImage displayImage, bufferImage;
    private int windowWidth, windowHeight;

    public DataCollection(int windowWidth, int windowHeight) {
        this.windowHeight = windowHeight;
        this.windowWidth = windowWidth;

        displayImage = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);
        bufferImage = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);

        initialize();
    }

    public void writeArray(int[] pixels, String fileName) {
    	System.out.println("been here");
        try {
            PrintStream ps = new PrintStream(new FileOutputStream("testdata/" + fileName));
            for (int pixel : pixels) {
                ps.println(pixel);
            }
            ps.close();
        } catch (IOException e) {
            System.out.println(e);
        }
        System.exit(0);
    }

    public int[] readArray(String filePath) {
        File dataFile = new File("testdata/" + filePath);
        List<Integer> tempArray = new ArrayList<>();
        try {
            fileScanner = new Scanner(dataFile);
        } catch (IOException e) {
            System.out.println(e);
        }
        while (fileScanner.hasNextLine()) {
            tempArray.add(Integer.parseInt(fileScanner.nextLine()));
        }
        fileScanner.close();
        return convertIntegers(tempArray);
    }

    public void paint(String filePath) {
        int[] renderedImage = readArray(filePath);

        if (renderedImage != null) {
            int[] displayPixels = ((DataBufferInt) bufferImage.getRaster().getDataBuffer()).getData();
            System.arraycopy(renderedImage, 0, displayPixels, 0, renderedImage.length);

            BufferedImage tempImage = displayImage;
            displayImage = bufferImage;
            bufferImage = tempImage;

            getGraphics().drawImage(displayImage, 0, 22, 912, 480, null);
        }
    }

    private void initialize() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(windowWidth, windowHeight + 25);
        setResizable(true);
        setVisible(true);
        setIgnoreRepaint(true);
    }

    private int[] convertIntegers(List<Integer> integers) {
        int[] intArray = new int[integers.size()];
        for (int i = 0; i < intArray.length; i++) {
            intArray[i] = integers.get(i);
        }
        return intArray;
    }

    public void drawPoint(int x, int y, int size, Color color) {
        Graphics g = this.getGraphics();
        g.setColor(color);
        g.fillRect(x, y, size, size);
    }
}

