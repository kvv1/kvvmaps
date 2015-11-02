package kvv.controllers.controller.adu;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class PacketTransceiver implements IPacketTransceiver {
	private final static int BAUD = 9600;

	private static final long BYTE_TIMEOUT = 100;

	private CommPortIdentifier pID;
	private SerialPort serPort;
	private InputStream inStream;
	private OutputStream outStream;
	private final int packetTimeout;
	
	private boolean closed;

	public PacketTransceiver(String comid, int packetTimeout) {
		this.packetTimeout = packetTimeout;
		try {
			System.out.println("init COM " + comid);
			pID = CommPortIdentifier.getPortIdentifier(comid);
			init();
		} catch (NoSuchPortException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void init() throws IOException {
		if(closed)
			throw new IOException("COM port is closed");
		
		if (serPort != null) {
			serPort.close();
			serPort = null;
		}

		try {
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
		} catch (PortInUseException | TooManyListenersException
				| UnsupportedCommOperationException e) {
			throw new IOException(e);
		}
	}

	public static void main(String[] args) throws NoSuchPortException,
			PortInUseException {
		CommPortIdentifier pID = CommPortIdentifier.getPortIdentifier("COM2");
		SerialPort serPort = (SerialPort) pID.open("PortReader", 2000);
		System.out.println(serPort.getInputBufferSize());
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

	private final BlockingQueue<Byte> queue = new LinkedBlockingQueue<>();

	private synchronized byte[] __sendPacket(byte[] data, boolean waitResponse,
			int packetTimeout) throws IOException {

		outStream.write(data);
		outStream.flush();

		long timeout = packetTimeout;

		List<Byte> buffer = new ArrayList<>();

		queue.clear();

		for (;;) {
			Byte b;
			try {
				b = queue.poll(timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				throw new IOException(e);
			}
			timeout = BYTE_TIMEOUT;
			if (b == null) {
				if (buffer.isEmpty())
					throw new PacketTimeoutException();
				else {
					byte[] res = new byte[buffer.size()];
					for (int i = 0; i < buffer.size(); i++)
						res[i] = buffer.get(i);
					return res;
				}
			}
			buffer.add(b);
		}
	}

	private class Listener implements SerialPortEventListener {
		@Override
		public void serialEvent(SerialPortEvent event) {
			if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
				try {
					while (inStream.available() > 0) {
						int ch = inStream.read();
						queue.add((byte) ch);
					}
				} catch (IOException | IllegalStateException e) {
					e.printStackTrace();
				}
			} else if (event.getEventType() == SerialPortEvent.OUTPUT_BUFFER_EMPTY) {
			}
		}
	}

	@Override
	public synchronized void close() {
		System.out.println("serPort closing");
		if (serPort != null)
			serPort.close();
		closed = true;
		System.out.println("serPort closed");
	}

}
