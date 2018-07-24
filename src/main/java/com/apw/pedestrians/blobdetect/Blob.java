package com.apw.pedestrians.blobdetect;

import com.apw.pedestrians.image.IPixel;

public class Blob {

  public static int currentId = 0;
  public int width, height;
  public int x, y;
  public IPixel color;
  public int id;

  public Blob(int width, int height, int x, int y, IPixel color) {
    set(width, height, x, y, color);
  }

  public void set(int width, int height, int x, int y, IPixel color) {
    set(width, height, x, y, color, currentId++);
  }

  //size, coordinates and color of blob
  public void set(int width, int height, int x, int y, IPixel color, int id) {
    this.width = width;
    this.height = height;
    this.x = x;
    this.y = y;
    this.color = color;
    this.id = id;
  }
}
