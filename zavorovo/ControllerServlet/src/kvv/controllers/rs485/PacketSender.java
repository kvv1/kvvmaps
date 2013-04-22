package kvv.controllers.rs485;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class PacketSender {
	private final static int BAUD = 9600;

	private static final long PACKET_TIMEOUT = 500;

	private static final long BYTE_TIMEOUT = 100;

	private CommPortIdentifier pID;
	private SerialPort serPort;
	private InputStream inStream;
	private OutputStream outStream;

	public PacketSender(String comid) throws Exception {
		pID = CommPortIdentifier.getPortIdentifier(comid);
		init();
	}

	private void init() throws Exception {
		if (serPort != null)
			serPort.close();

		serPort = (SerialPort) pID.open("PortReader", 2000);
		inStream = serPort.getInputStream();
		outStream = serPort.getOutputStream();
		serPort.notifyOnDataAvailable(true);
		serPort.notifyOnOutputEmpty(true);
		serPort.addEventListener(new Listener());
		serPort.setSerialPortParams(BAUD, SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		serPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
		serPort.setRTS(true);
	}

	private List<Byte> inputBuffer = new ArrayList<Byte>();
	private long lastReceiveTime;
	private long sendTime;

	public synchronized byte[] sendPacket(byte[] data, boolean waitResponse)
			throws IOException {
		try {
			return _sendPacket(data, waitResponse);
		} catch (Exception e) {
			try {
				init();
			} catch (Exception e1) {
				throw new IOException(e1);
			}
			throw new IOException(e);
		}
	}

	private synchronized byte[] _sendPacket(byte[] data, boolean waitResponse)
			throws Exception {
		synchronized (inputBuffer) {
			inputBuffer.clear();
		}

		sendTime = System.currentTimeMillis();
		outStream.write(data);
		outStream.flush();

		if (!waitResponse)
			return null;

		while (true) {
			long time = System.currentTimeMillis();
			if (time > sendTime + PACKET_TIMEOUT)
				throw new IOException("PACKET_TIMEOUT");
			if (!inputBuffer.isEmpty() && time > lastReceiveTime + BYTE_TIMEOUT) {
				byte[] response = new byte[inputBuffer.size()];
				for (int i = 0; i < inputBuffer.size(); i++)
					response[i] = inputBuffer.get(i);
				return response;
			}
			Thread.sleep(2);
		}
	}

	private class Listener implements SerialPortEventListener {
		@Override
		public void serialEvent(SerialPortEvent event) {
			if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
				try {
					while (inStream.available() > 0) {
						int ch = inStream.read();
						synchronized (inputBuffer) {
							inputBuffer.add((byte) ch);
							lastReceiveTime = System.currentTimeMillis();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (event.getEventType() == SerialPortEvent.OUTPUT_BUFFER_EMPTY) {
			}
		}
	}

	public void close() {
		serPort.close();
	}
}