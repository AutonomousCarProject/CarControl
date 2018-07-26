package com.apw.fakefirm;

import jssc.SerialPortException;

public interface PortObject {
	public boolean openPort() throws SerialPortException;
	public boolean closePort() throws SerialPortException;
	public boolean writeBytes(byte[] buffer) throws SerialPortException;
	public boolean setParams(int baudRate, int dataBits, int stopBits, int parity) throws SerialPortException;
}
