package com.apw.pinio.raspi;

import com.apw.pinio.PWMController;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

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

  @Override
  public void pinMode(int pin, Protocol protocol, IO io) {

  }

  @Override
  public void write(int pin, DataPackage dataPackage) {

  }

  @Override
  public void writeUnchecked(int pin, DataPackage dataPackage) {

  }

  @Override
  public DataPackage read(int pin) {
    return null;
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
