package com.apw.pwm.raspi;

import com.apw.pwm.PWMController;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;

public class RaspiPWM implements PWMController {

  private static final RaspiPWM theController = new RaspiPWM();

  private final GpioController gpioController = GpioFactory.getInstance();

  private RaspiPWM() {

  }

  // Implicit override
  public static RaspiPWM getInstance() {
    return theController;
  }

  @Override
  public boolean IsOpen() {
    return gpioController.isShutdown();
  }

  // TODO wtf are the mode and value bytes actually representing

  @Override
  public void pinMode(int pin, byte mode) {

  }

  @Override
  public void digitalWrite(int pin, byte value) {
    // TODO real implementation
    gpioController.provisionDigitalOutputPin(RaspiPin.getPinByAddress(pin)).pulse(value);
  }

  @Override
  public void servoWrite(int pin, int angle) {
    // TODO real implementation
    gpioController.provisionDigitalOutputPin(RaspiPin.getPinByAddress(pin)).pulse(angle);
  }

  @Override
  public void Open() {
    // TODO figure out why this is even necessary instead of just being called in the constructor
  }

  @Override
  public void Close() {
    gpioController.shutdown();
  }
}
