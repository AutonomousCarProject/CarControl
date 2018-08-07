package com.apw.pedestrians.image;

//Defines basic implementation for pixel
public class Pixel {
    private Color color;

    public Pixel(Color color) {
        this.setColor(color);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}