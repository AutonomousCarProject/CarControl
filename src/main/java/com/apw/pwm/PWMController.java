package com.apw.pwm;

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
    System.err.println("Error: singleton accessor was not overriden by member.")
    return null;
  }

  /**
   * Current status of the FakeFirmata library, =true if successfully open.
   *
   * @return true if successfully open, false if failed or closed
   */
  boolean IsOpen() // true if opened successfully
  ;

  /**
   * Sets the mode of the specified pin (INPUT or OUTPUT).
   *
   * @param pin The arduino pin.
   * @param mode Mode ArduinoPWM.OUTPUT or ArduinoPWM.SERVO
   * (ArduinoPWM.INPUT, ArduinoPWM.ANALOG or ArduinoPWM.PWM not supported)
   */
  void pinMode(int pin, byte mode) //~pinMode
  ;

  /**
   * Write to a digital pin that has been toggled to output mode
   * with pinMode() method.
   *
   * @param pin The digital pin to write to.
   * @param value Value either ArduinoPWM.LOW or ArduinoPWM.HIGH.
   */
  void digitalWrite(int pin, byte value) //~digitalWrite
  ;

  /**
   * [For] controlling [a] servo.
   *
   * @param pin Servo output pin.
   */
  void servoWrite(int pin, int angle) //~servoWrite
  ;

  /**
   * Opens the serial port connection, should it be required.
   * By default the port is opened when the object is first created.
   * <p>
   * Note that JSSC does not recover gracefully from a failure to close,
   * so a subsequent Open() will fail until the system is rebooted.
   */
  void Open() //~Open
  ;

  /**
   * Closes the serial port.
   * <p>
   * Note that JSSC does not recover gracefully from a failure to close,
   * so a subsequent Open() will fail until the system is rebooted.
   */
  void Close() //~Close
  ;
}
