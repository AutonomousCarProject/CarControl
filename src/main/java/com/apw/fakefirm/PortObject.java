package com.apw.fakefirm;

import jssc.SerialPortException;

public interface PortObject {
	
	public boolean openPort() throws SerialPortException;

	public boolean closePort() throws SerialPortException;
	
	public boolean writeBytes(byte[] buffer) throws SerialPortException;
	
	public byte[] readBytes(int byteCount) throws SerialPortException;
	
	public int getInputBufferBytesCount() throws SerialPortException; //returns amount of bytes in input buffer
	
	public boolean setParams(int baudRate, int dataBits, int stopBits, int parity) throws SerialPortException;

}
