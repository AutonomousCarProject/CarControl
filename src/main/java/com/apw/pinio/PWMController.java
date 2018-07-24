package com.apw.pinio;

/**
 * Members must conform to a singleton pattern.
 */
public interface PWMController {

  /**
   * Singleton accessor. This method must be overriden by the member.
   *
   * @return The single instance of the PWMController.
   */
  static PWMController getInstance() {
    System.err.println("Error: singleton accessor was not overriden by member.");
    return null;
  }

  boolean IsOpen();

  void pinMode(int pin, Protocol protocol, IO io);

  void write(int pin, DataPackage dataPackage);

  void writeUnchecked(int pin, DataPackage dataPackage);

  DataPackage read(int pin);

  /**
   * Opens the serial port connection, should it be required.
   * By default the port is opened when the object is first created.
   * <p>
   * Note that JSSC does not recover gracefully from a failure to close,
   * so a subsequent Open() will fail until the system is rebooted.
   */
  void Open();

  /**
   * Closes the serial port.
   * <p>
   * Note that JSSC does not recover gracefully from a failure to close,
   * so a subsequent Open() will fail until the system is rebooted.
   */
  void Close();

  enum Protocol {
    DIGITAL, ANALOG, SERVO, PWM
  }

  enum IO {
    INPUT, OUTPUT
  }

  class DataPackage {
    // TODO
  }

}
