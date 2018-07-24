package com.apw.pwm.raspi;

import com.apw.pwm.PWMController;

public class RaspiPWM implements PWMController {

  @Override
  public boolean IsOpen() {
    return false;
  }

  @Override
  public void pinMode(int pin, byte mode) {

  }

  @Override
  public void digitalWrite(int pin, byte value) {

  }

  @Override
  public void servoWrite(int pin, int angle) {

  }

  @Override
  public void Open() {

  }

  @Override
  public void Close() {

  }
}
