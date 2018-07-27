package com.apw.pedestrians.image;

public interface IPixel {

  // 0 = red, 1 = green, 2 = blue, 3 = gray, 4 = black, 5 = white
  Color getColor();

  void setColor(Color c);
}