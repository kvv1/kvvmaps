package kvv.heliostat.server.controller.adu;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import kvv.heliostat.server.envir.controller.BusLogger;

public class PacketTransceiver implements IPacketTransceiver {
	private final static int BAUD = 9600;

	private static final long BYTE_TIMEOUT = 100;

	private final CommPortIdentifier pID;
	private SerialPort serPort;
	private InputStream inStream;
	private OutputStream outStream;
	private List<Byte> inputBuffer = new ArrayList<Byte>();
	private long lastReceiveTime;
	private long sendTime;
	private final int packetTimeout;

	public PacketTransceiver(String comid, int packetTimeout) throws IOException {
		try {
			pID = CommPortIdentifier.getPortIdentifier(comid);
			this.packetTimeout = packetTimeout;
			init();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	private void init() throws Exception {

		if (serPort != null) {
			serPort.close();
			serPort = null;
		}

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

	public static void main(String[] args) throws NoSuchPortException,
			PortInUseException {
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

	@Override
	public synchronized byte[] sendPacket(byte[] data, boolean waitResponse)
			throws IOException {
		try {
			return __sendPacket(data, waitResponse, packetTimeout);
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

	private synchronized byte[] __sendPacket(byte[] data, boolean waitResponse,
			int packetTimeout) throws IOException {

		synchronized (inputBuffer) {
			if (!inputBuffer.isEmpty()) {
				String msg = "inputBuffer: ";
				for (byte b : inputBuffer)
					msg += Integer.toHexString((int) b & 0xFF) + " ";
				BusLogger.log(msg);
				System.err.println(msg);
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
			if (System.currentTimeMillis() > sendTime + packetTimeout)
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
					for (int i = 0; i < inputBuffer.size(); i++) {
						response[i] = inputBuffer.get(i);
						System.err.print(Integer.toHexString((int) response[i] & 0xFF) + " ");
					}
					System.err.println();
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

	@Override
	public void close() {
		serPort.close();
	}

}
