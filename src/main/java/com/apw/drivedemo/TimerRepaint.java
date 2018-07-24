package com.apw.drivedemo;

import java.util.TimerTask;

public abstract class TimerRepaint extends TimerTask {

  protected DriveTest window;

  public TimerRepaint(DriveTest window) {
    super();
    this.window = window;
  }
}
