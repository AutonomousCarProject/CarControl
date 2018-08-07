package com.apw.imagemanagement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SimpleThresholds extends JFrame implements ActionListener {
    public static final boolean simpleSet = false;
    public JButton applyButton;
    public JTextField redLabel;
    public JTextField greenLabel;
    public JTextField blueLabel;
    public JTextField yellowLabel;
    public JTextField whiteLabel;
    public JTextField greyLabel;
    public JTextField redComp;
    public JTextField greenComp;
    public JTextField blueComp;
    //*  Simple
    public JTextField redSet;
    public JTextField blueSet;
    public JTextField greenSet;
    public JTextField whiteSet;
    public JTextField greySet;
    //*/
    //*  Complex
    public JTextField redOfBlue;
    public JTextField redOfGreen;
    public JTextField blueOfRed;
    public JTextField blueOfGreen;
    public JTextField greenOfRed;
    public JTextField greenOfBlue;
    public JTextField yellowRGDifference;
    public JTextField yellowOfBlue;
    //*/
    public static int redGreen = 50;
    public static int redBlue = 50;
    public static int greenRed = 40;
    public static int greenBlue = -10;
    public static int blueRed = 50;
    public static int blueGreen = 50;
    public static int yellowDiff = 25;
    public static int yellowBlue = 70;
    public static int whitePoint = 140;
    public static int greyPoint = 85;




    public SimpleThresholds(){
        this.setMinimumSize(new Dimension(600,600));
        //GridBagLayout lay= new GridBagLayout();
        setLayout( new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx=1;
        c.gridy=0;
        redLabel = new JTextField("RED");
        redLabel.setEditable(false);
        add(redLabel,c);
        greenLabel = new JTextField("GREEN");
        greenLabel.setEditable(false);
        c.gridx=2;
        add(greenLabel,c);
        blueLabel = new JTextField("BLUE");
        blueLabel.setEditable(false);
        c.gridx=3;
        add(blueLabel,c);
        yellowLabel = new JTextField("YELLOW");
        yellowLabel.setEditable(false);
        c.gridx=4;
        add(yellowLabel,c);
        whiteLabel = new JTextField("WHITE");
        whiteLabel.setEditable(false);
        c.gridx=5;
        add(whiteLabel,c);
        greyLabel = new JTextField("GREY");
        greyLabel.setEditable(false);
        c.gridx=6;
        add(greyLabel,c);
        redComp = new JTextField("RED");
        redComp.setEditable(false);
        c.gridx=0;
        c.gridy=1;
        add(redComp,c);
        greenComp = new JTextField("GREEN");
        greenComp.setEditable(false);
        c.gridy=2;
        add(greenComp,c);
        blueComp = new JTextField("Blue");
        blueComp.setEditable(false);
        c.gridy=3;
        add(blueComp,c);
        c.gridx=0;
        c.gridy=0;
        applyButton = new JButton("APPLY");
        applyButton.addActionListener(this);
        add(applyButton,c);
        if(simpleSet){
            redSet = new JTextField("100");
            c.gridx=1;
            c.gridy=1;
            add(redSet,c);
            blueSet = new JTextField("100");
            c.gridx=2;
            add(blueSet,c);
            greenSet = new JTextField("100");
            c.gridx=3;
            add(greenSet,c);
        }else{
            redOfBlue = new JTextField("100");
            c.gridx=1;
            c.gridy=3;
            add(redOfBlue,c);
            redOfGreen = new JTextField("100");
            c.gridy=2;
            add(redOfGreen,c);
            greenOfRed = new JTextField("100");
            c.gridx=2;
            c.gridy=1;
            add(greenOfRed,c);
            greenOfBlue = new JTextField("100");
            c.gridy=3;
            add(greenOfBlue,c);
            blueOfRed = new JTextField("100");
            c.gridx=3;
            c.gridy=1;
            add(blueOfRed,c);
            blueOfGreen = new JTextField("100");
            c.gridy=2;
            add(blueOfGreen,c);

        }
        yellowRGDifference = new JTextField("100");
        c.gridx=4;
        c.gridy=2;
        add(yellowRGDifference,c);
        yellowOfBlue = new JTextField("100");
        c.gridy=3;
        add(yellowOfBlue,c);
        whiteSet = new JTextField("100");
        c.gridx=5;
        c.gridy=1;
        add(whiteSet,c);
        greySet = new JTextField("100");
        c.gridx=6;
        add(greySet,c);
        //JFrame temp = new JFrame();
        //temp.setMinimumSize(new Dimension(600,600));
        //temp.add(this);
        //temp.setVisible(true);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (simpleSet) {
                redGreen = Integer.parseInt(redSet.getText());
                redBlue = redGreen;
                greenBlue = Integer.parseInt(greenSet.getText());
                greenRed = greenBlue;
                blueRed = Integer.parseInt(blueSet.getText());
                blueGreen = blueRed;
            }else{
                redGreen = Integer.parseInt(redOfGreen.getText());
                redBlue = Integer.parseInt(redOfBlue.getText());
                greenRed = Integer.parseInt(greenOfRed.getText());
                greenBlue = Integer.parseInt(greenOfBlue.getText());
                blueRed = Integer.parseInt(blueOfRed.getText());
                blueGreen = Integer.parseInt(blueOfGreen.getText());
            }
            yellowDiff = Integer.parseInt(yellowRGDifference.getText());
            yellowBlue = Integer.parseInt(yellowOfBlue.getText());
            whitePoint = Integer.parseInt(whiteSet.getText());
            greyPoint = Integer.parseInt(greySet.getText());
        }catch(Exception ex){
            System.err.println(ex);
        }
    }
}
