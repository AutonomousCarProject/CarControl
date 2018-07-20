/* FakeFirmata -- a simple way to control servos from LattePanda in Java.
 *
 * This is essentially a translation of (small parts of) LattePanda's Arduino.cs
 * into Java for using the attached Arduino to control servos.
 *
 * Under US Copyright law this miniscule copy counts as "Fair Use" and in the
 * public domain, but if you are worried about it, or if you extend it to
 * include more of their code, then you will be bound by the onerous rules of
 * the GNU General Public License or whatever they are currently using.
 *
 * FakeFirmata is designed to work with JSSC (Java Simple Serial Connector),
 * but probably will work with any compatible Java serial port API.
 */
package com.apw.fakefirm;                                     // 2018 February 10

// import nojssc.SerialPort; // use this instead for working with TrackSim
//                           // ..on a computer with no serial port.

import com.apw.noJSSC.SerialPort;

public class Arduino { // Adapted to Java from arduino.cs ... (FakeFirmata)
    // (subclass this to add input capability)

    public static final String CommPortNo = "COM3";
    public static final int MAX_DATA_BYTES = 16, // =64 in LattePanda's Arduino.cs
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
    protected
    SerialPort surrealPort;

    public Arduino() { // outer class constructor..
        surrealPort = new SerialPort(CommPortNo);
        System.out.println("new Arduino " + CommPortNo + " " + (surrealPort != null));
        digitalOutputData = new int[MAX_DATA_BYTES];
        Open();
    }

    /**
     * Use this to link simulator actions to Firmata.
     *
     * @param whom An instance of a subclass of SimHookBase
     */
    public static void HookExtend(SimHookBase whom) {
        DoMore = whom;
    }

    /**
     * Current status of the FakeFirmata library, =true if successfully open.
     *
     * @return true if successfully open, false if failed or closed
     */
    public boolean IsOpen() {
        return GoodOpen;
    } // true if opened successfully

    /**
     * Sets the mode of the specified pin (INPUT or OUTPUT).
     *
     * @param pin  The arduino pin.
     * @param mode Mode Arduino.OUTPUT or Arduino.SERVO
     *             (Arduino.INPUT, Arduino.ANALOG or Arduino.PWM not supported)
     */
    public void pinMode(int pin, byte mode) {
        byte[] msg = new byte[3];
        if (SpeakEasy) System.out.println("F%%F/pinMode +" + pin + " = " + mode);
        msg[0] = (byte) (SET_PIN_MODE);
        msg[1] = (byte) (pin);
        msg[2] = (byte) (mode);
        try {
            surrealPort.writeBytes(msg);
            if (DoMore != null) DoMore.SendBytes(msg, 3);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    } //~pinMode

    /**
     * Write to a digital pin that has been toggled to output mode
     * with pinMode() method.
     *
     * @param pin   The digital pin to write to.
     * @param value Value either Arduino.LOW or Arduino.HIGH.
     */
    public void digitalWrite(int pin, byte value) {
        int portNumber = (pin >> 3) & 0x0F;
        int digiData = digitalOutputData[portNumber & MDB_msk];
        byte[] msg = new byte[3];
        if (SpeakEasy) System.out.println("F%%F/digiWrite +" + pin + " = " + value);
        if ((int) value == 0)
            digiData = digiData & ~(1 << (pin & 0x07));
        else digiData = digiData | (1 << (pin & 0x07));
        digitalOutputData[portNumber & MDB_msk] = digiData;
        msg[0] = (byte) (DIGITAL_MESSAGE | portNumber);
        msg[1] = (byte) (digiData & 0x7F);
        msg[2] = (byte) (digiData >> 7);
        try {
            surrealPort.writeBytes(msg);
            if (DoMore != null) DoMore.SendBytes(msg, 3);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    } //~digitalWrite

    /**
     * [For] controlling [a] servo.
     *
     * @param pin Servo output pin.
     */
    public void servoWrite(int pin, int angle) {
        byte[] msg = new byte[3];
        if (SpeakEasy) System.out.println("F%%F/servoWrite +" + pin + " = " + angle);
        msg[0] = (byte) (ANALOG_MESSAGE | (pin & 0x0F));
        msg[1] = (byte) (angle & 0x7F);
        msg[2] = (byte) (angle >> 7);
        try {
            surrealPort.writeBytes(msg);
            if (DoMore != null) DoMore.SendBytes(msg, 3);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    } //~servoWrite

    /**
     * Opens the serial port connection, should it be required.
     * By default the port is opened when the object is first created.
     * <p>
     * Note that JSSC does not recover gracefully from a failure to close,
     * so a subsequent Open() will fail until the system is rebooted.
     */
    public void Open() {
        if (SpeakEasy) System.out.println("F%%F/Open..");
        if (GoodOpen) {
            if (SpeakEasy) System.out.println("... " + CommPortNo + " is already open");
            return;
        } else try {
            GoodOpen = surrealPort.openPort();
            surrealPort.setParams(57600, 8, 1, 0);
        } catch (Exception ex) {
            System.out.println(ex);
        }
        if (SpeakEasy) {
            if (GoodOpen) System.out.println("... " + CommPortNo + " is now open");
            else System.out.println("... " + CommPortNo + " failed to open");
        }
    } //~Open

    /**
     * Closes the serial port.
     * <p>
     * Note that JSSC does not recover gracefully from a failure to close,
     * so a subsequent Open() will fail until the system is rebooted.
     */
    public void Close() {
        if (SpeakEasy) System.out.println("F%%F/Close..");
        if (GoodOpen) try {
            surrealPort.closePort();
            DoMore = null;
        } catch (Exception ex) {
            System.out.println(ex);
        }
        GoodOpen = false;
    } //~Close
} //~Arduino (fakefirm) (OO)
