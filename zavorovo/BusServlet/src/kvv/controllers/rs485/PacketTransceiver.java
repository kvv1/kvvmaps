package kvv.controllers.rs485;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import kvv.controllers.controller.BusLogger;

public class PacketTransceiver {
	private final static int BAUD = 9600;

	private static final long PACKET_TIMEOUT = 400;

	private static final long BYTE_TIMEOUT = 100;

	private CommPortIdentifier pID;
	private SerialPort serPort;
	private InputStream inStream;
	private OutputStream outStream;
	private List<Byte> inputBuffer = new ArrayList<Byte>();
	private long lastReceiveTime;
	private long sendTime;

	private static PacketTransceiver instance;

	public static synchronized PacketTransceiver getInstance() throws Exception {
		if (instance == null) {
			Properties props = new Properties();
			FileInputStream is = new FileInputStream(
					"c:/zavorovo/controller.properties");
			props.load(is);
			is.close();
			String com = props.getProperty("COM");
			if (com == null)
				return null;
			instance = new PacketTransceiver(com);
		}
		return instance;
	}

	public static synchronized void closeInstance() {
		if (instance != null)
			instance.close();
		instance = null;
	}

	public PacketTransceiver(String comid) throws Exception {
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

	public static void main(String[] args) throws NoSuchPortException, PortInUseException {
		CommPortIdentifier pID = CommPortIdentifier.getPortIdentifier("COM2");
		SerialPort serPort = (SerialPort) pID.open("PortReader", 2000);
		System.out.println(serPort.getInputBufferSize());
		
	}
	
	class PacketTimeoutException extends IOException {
		private static final long serialVersionUID = 1L;

		public PacketTimeoutException() {
			super("PACKET_TIMEOUT");
		}
	}

	public synchronized byte[] sendPacket(byte[] data, boolean waitResponse)
			throws IOException {
		try {
			return __sendPacket(data, waitResponse);
		} catch (PacketTimeoutException e) {
			throw e;
		} catch (IOException e) {
			try {
				Thread.sleep(500);
				init();
			} catch (Exception e1) {
				throw new IOException(e1);
			}
			throw e;
		}
	}

	private synchronized byte[] __sendPacket(byte[] data, boolean waitResponse)
			throws IOException {

		synchronized (inputBuffer) {
			if (!inputBuffer.isEmpty()) {
				String msg = "inputBuffer: ";
				for (byte b : inputBuffer)
					msg += Integer.toHexString((int) b & 0xFF) + " ";
				BusLogger.log(msg);
			}

			inputBuffer.clear();
		}

		sendTime = System.currentTimeMillis();
		outStream.write(data);
		outStream.flush();

		if (!waitResponse)
			return null;

		while (true) {
			synchronized (inputBuffer) {
				if (!inputBuffer.isEmpty())
					break;
			}
			if (System.currentTimeMillis() > sendTime + PACKET_TIMEOUT)
				throw new PacketTimeoutException();
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
			}
		}

		while (true) {
			synchronized (inputBuffer) {
				if (!inputBuffer.isEmpty()
						&& System.currentTimeMillis() > lastReceiveTime
								+ BYTE_TIMEOUT) {
					byte[] response = new byte[inputBuffer.size()];
					for (int i = 0; i < inputBuffer.size(); i++)
						response[i] = inputBuffer.get(i);
					inputBuffer.clear();
					return response;
				}
			}
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
			}
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
