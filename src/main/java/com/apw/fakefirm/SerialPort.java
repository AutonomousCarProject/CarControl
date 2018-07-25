/* SerialPort -- a substitute class with the same API but does nothing.
 *
 * FakeFirmata is designed to work with JSSC (Java Simple Serial Connector),
 * but when using TrakSim, all you need are stubs with the same API.
 */
package com.apw.fakefirm;                                 // 2018 May 11

/**
 * This class has the same name & API as JSSC but does nothing.
 */
public class SerialPort {
    static final boolean logo = true; // enable logging..

    public SerialPort() {
        if (logo) System.out.println("noJSC/new SerialPort");
    }

    public SerialPort(String myPortName) { // constructor..
        if (logo) System.out.println("noJSC/new SerialPort '"
                + myPortName + "'");
    }

    /**
     * Port opening
     *
     * @return true
     */
    public boolean openPort() {
        if (logo) System.out.println("noJSC/openPort");
        return true;
    }

    /**
     * Setting the parameters of port.
     *
     * @param baudRate data transfer rate
     * @param dataBits number of data bits
     * @param stopBits number of stop bits
     * @param parity   parity
     * @return true
     */
    public boolean setParams(int baudRate, int dataBits, int stopBits, int parity) {
        if (logo) System.out.println("noJSC/setParams " + baudRate + " " + dataBits
                + " " + stopBits + " " + parity);
        return true;
    } //~setParams

    /**
     * Write byte array to port
     *
     * @param buffer the byte array to write
     * @return true
     */
    public boolean writeBytes(byte[] buffer) {
        if (buffer == null) return false;
//ABCDE
//        if (logo) System.out.println("noJSC/writeBytes " + buffer[0]);
        return true;
    } //~writeBytes

    /**
     * Close port. This method pretends to close the port
     *
     * @return true
     */
    public boolean closePort() {
        if (logo) System.out.println("noJSC/closePort");
        return true;
    } //~closePort
} //~SerialPort (nojssc) (NS)
