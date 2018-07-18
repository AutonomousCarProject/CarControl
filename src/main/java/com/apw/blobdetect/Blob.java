package com.apw.blobdetect;

import com.apw.oldimage.IPixel;

public class Blob
{
    public int width, height;
    public int x, y;
    public IPixel color;
    public int id;

    public static int currentId = 0;

    public Blob(int width, int height, int x, int y, IPixel color)
    {
        set(width, height, x, y, color);
    }

    public void set(int width, int height, int x, int y, IPixel color)
    {
        set(width, height, x, y, color, currentId++);
    }

    //size, coordinates and color of blob
    public void set(int width, int height, int x, int y, IPixel color, int id)
    {
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.color = color;
        this.id = id;
    }
}
