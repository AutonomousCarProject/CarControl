package com.apw.sbcio;

/**
 * Interface dedicated to communication with motors and receiving information from hardware
 * Members must conform to a singleton pattern.
 * 
 * @author I don't know your name
 * @author Colton Jelsema
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

  /**
   * Set the angle of a servo. By default, calls {@link #setOutputPulseWidth(int, double)}
   * normalized to 1-2 ms.
   *
   * @param pin Non-negative integer representing the pin number. Range dependent on
   * implementation.
   * @param angle int from 0 to 180 inclusive representing the angle of the servo.
   */
  default void setServoAngle(int pin, int angle) {
    setOutputPulseWidth(pin, 1 + angle / 180);
  }
  
  /**
   * Write three byte-castable ints to the buffer
   * 
   * @param i First byte
   * @param j Second byte
   * @param k Third byte
   */
  void Write(int i, int j, int k);
  
  
  /**
   * Set the pulse width of the output.
   *
   * @param pin Non-negative integer representing the pin number. Range dependent on
   * implementation.
   * @param ms The width of the pulse in milliseconds.
   */
  void setOutputPulseWidth(int pin, double ms);

  
  void close();

  
  /**
   * Get any info from the buffer
   * 
   * @return undecided
   */
  void digitalRead();



}
