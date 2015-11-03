package kvv.controllers.controller.adu;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class COMTransceiver extends ModbusPacketTransceiver implements
		IPacketTransceiver {
	private final static int BAUD = 9600;
	private final String com;
	private final int packetTimeout;
	private InputStream inStream;
	private OutputStream outStream;
	private SerialPort serPort;
	private boolean closed;

	public COMTransceiver(String com, int packetTimeout) {
		this.com = com;
		this.packetTimeout = packetTimeout;
	}

	@Override
	public synchronized byte[] sendPacket(byte[] data, boolean waitResponse)
			throws IOException {
		
		if (closed)
			throw new IOException("COM port is closed");

		if (inStream == null) {

			try {
				CommPortIdentifier pID = CommPortIdentifier
						.getPortIdentifier(com);
				serPort = (SerialPort) pID.open("PortReader", 2000);
				inStream = serPort.getInputStream();
				outStream = serPort.getOutputStream();
				serPort.setSerialPortParams(BAUD, SerialPort.DATABITS_8,
						SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				serPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
				serPort.setRTS(true);
			} catch (Exception e) {
				throw new IOException(e);
			}
		}

		try {
			return sendPacket(data, inStream, outStream, waitResponse,
					packetTimeout);
		} catch (PacketTimeoutException e) {
			throw e;
		} catch (IOException e) {
			if (serPort != null)
				serPort.close();
			serPort = null;
			inStream = null;
			outStream = null;
			throw e;
		}

	}

	@Override
	public synchronized void close() {
		closed = true;
		if (serPort != null) {
			serPort.close();
			serPort = null;
			inStream = null;
			outStream = null;
		}
	}

}
