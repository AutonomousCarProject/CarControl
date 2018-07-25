/* FakeFirmata -- a simple way to control servos from LattePanda in Java.
 *
 * This is essentially a translation of (small parts of) LattePanda's ArduinoPWM.cs
 * into Java for using the attached ArduinoPWM to control servos.
 *
 * Under US Copyright law this miniscule copy counts as "Fair Use" and in the
 * public domain, but if you are worried about it, or if you extend it to
 * include more of their code, then you will be bound by the onerous rules of
 * the GNU General Public License or whatever they are currently using.
 *
 * FakeFirmata is designed to work with JSSC (Java Simple Serial Connector),
 * but probably will work with any compatible Java serial port API.
 */
package com.apw.pwm.fakefirm;                                     // 2018 February 10

// import nojssc.SerialPort; // use this instead for working with TrackSim
//                           // ..on a computer with no serial port.

import com.apw.nojssc.SerialPort;
import com.apw.pwm.PWMController;

public class ArduinoPWM implements
    PWMController { // Adapted to Java from arduino.cs ... (FakeFirmata)
  // (subclass this to add input capability)

  private static final ArduinoPWM theController = new ArduinoPWM();

  public static final String CommPortNo = "COM3";
  public static final int MAX_DATA_BYTES = 16, // =64 in LattePanda's ArduinoPWM.cs
      MDB_msk = MAX_DATA_BYTES - 1;
  public static final int SET_PIN_MODE = 0xF4;
  // set a pin to INPUT/OUTPUT/PWM/etc
  public static final int DIGITAL_MESSAGE = 0x90;
  // send data for a digital port
  public static final int ANALOG_MESSAGE = 0xE0;
  // Use these selectors to set listeners..
  public static final int REPORT_ANALOG = 0xC0;
  // enable analog input by pin +
  public static final int REPORT_DIGITAL = 0xD0;
  // send data for an analog pin (or PWM)
  // enable digital input by port
  public static final int REPORT_VERSION = 0xF9;
  public static final byte LOW = 0;
  public static final byte HIGH = 1;
  // report firmware version
  public static final byte INPUT = 0;
  public static final byte OUTPUT = 1;
  public static final byte ANALOG = 2;
  public static final byte PWM = 3;
  public static final byte SERVO = 4;
  protected static final boolean SpeakEasy = true;
  protected static boolean GoodOpen = false;
  protected static SimHookBase DoMore = null; // for extensions

  protected int[] digitalOutputData;
  protected SerialPort surrealPort;

  private ArduinoPWM() { // outer class constructor..
    surrealPort = new SerialPort(CommPortNo);
    System.out.println("new ArduinoPWM " + CommPortNo + " " + (surrealPort != null));
    digitalOutputData = new int[MAX_DATA_BYTES];
    open();
  }

  // Implicit override
  public static ArduinoPWM getInstance() {
    return theController;
  }

  /**
   * Use this to link simulator actions to Firmata.
   *
   * @param whom An instance of a subclass of SimHookBase
   */
  public static void HookExtend(SimHookBase whom) {
    DoMore = whom;
  }

  public boolean isOpen() {
    return GoodOpen;
  } // true if opened successfully

  public void pinMode(int pin, byte mode) {
    byte[] msg = new byte[3];
    if (SpeakEasy) {
      System.out.println("F%%F/setPinMode +" + pin + " = " + mode);
    }
    msg[0] = (byte) (SET_PIN_MODE);
    msg[1] = (byte) (pin);
    msg[2] = mode;
    try {
      surrealPort.writeBytes(msg);
      if (DoMore != null) {
        DoMore.SendBytes(msg, 3);
      }
    } catch (Exception ex) {
      System.out.println(ex);
    }
  } //~setPinMode

  @Override
  public void setServoAngle(int pin, double angle) {

    // Temporary patch until this function properly supports doubles
    int roundedAngle = Math.toIntExact(Math.round(angle));

    byte[] msg = new byte[3];
//ABCDE
//        if (SpeakEasy) System.out.println("F%%F/servoWrite +" + pin + " = " + angle);
    msg[0] = (byte) (ANALOG_MESSAGE | (pin & 0x0F));
    msg[1] = (byte) (roundedAngle & 0x7F);
    msg[2] = (byte) (roundedAngle >> 7);
    try {
      surrealPort.writeBytes(msg);
      if (DoMore != null) {
        DoMore.SendBytes(msg, 3);
      }
    } catch (Exception ex) {
      System.out.println(ex);
    }
  } //~servoWrite

  public void setOutputPulseWidth(int pin, double ms) {
    // TODO break out from setServoAngle
  }

  private void open() {
    if (SpeakEasy) {
      System.out.println("F%%F/Open..");
    }
    if (GoodOpen) {
      if (SpeakEasy) {
        System.out.println("... " + CommPortNo + " is already open");
      }
      return;
    } else {
      try {
        GoodOpen = surrealPort.openPort();
        surrealPort.setParams(57600, 8, 1, 0);
      } catch (Exception ex) {
        System.out.println(ex);
      }
    }
    if (SpeakEasy) {
      if (GoodOpen) {
        System.out.println("... " + CommPortNo + " is now open");
      } else {
        System.out.println("... " + CommPortNo + " failed to open");
      }
    }
  } //~Open

  @Override
  public void close() {
    if (SpeakEasy) {
      System.out.println("F%%F/Close..");
    }
    if (GoodOpen) {
      try {
        surrealPort.closePort();
        DoMore = null;
      } catch (Exception ex) {
        System.out.println(ex);
      }
    }
    GoodOpen = false;
  } //~Close
} //~ArduinoPWM (fakefirm) (OO)
