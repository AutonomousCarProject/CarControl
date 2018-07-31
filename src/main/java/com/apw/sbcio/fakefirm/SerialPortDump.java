/* SerialPort -- a substitute class with the same API but does nothing.
 *
 * FakeFirmata is designed to work with JSSC (Java Simple Serial Connector),
 * but when using TrakSim, all you need are stubs with the same API.
 */
package com.apw.sbcio.fakefirm;                                 // 2018 May 11

import jssc.SerialPortException;

/**
 * This class has the same name & API as JSSC but does nothing.
 */
public class SerialPortDump implements PortObject {
  static final boolean logo = true; // enable logging..

  public SerialPortDump() {
    if (logo) System.out.println("noJSC/new SerialPort");
  }

  public SerialPortDump(String myPortName) { // constructor..
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

  @Override
  public byte[] readBytes(int byteCount) throws SerialPortException {
    // TODO Auto-generated method stub
    System.out.println("noJSC/ You shouldn't be trying to read bytes without anything to read!");
    return null;
  }

  @Override
  public int getInputBufferBytesCount() throws SerialPortException {
    // TODO Auto-generated method stub
    return 0;
  }
} //~SerialPort (nojssc) (NS)