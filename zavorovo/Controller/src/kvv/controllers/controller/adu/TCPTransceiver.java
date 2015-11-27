package kvv.controllers.controller.adu;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TCPTransceiver extends ModbusPacketTransceiver implements
		IPacketTransceiver {
	private final int port;
	private final String host;

	private volatile Socket s;
	private InputStream inputStream;
	private OutputStream outputStream;
	private volatile boolean closed;

	public TCPTransceiver(String host, int port) {
		this.host = host;
		this.port = port;
		thread.start();
	}

	@Override
	public synchronized byte[] sendPacket(byte[] data, boolean waitResponse, int packetTimeout)
			throws IOException {

		if (closed)
			throw new IOException("COM port is closed");

		if (inputStream == null)
			throw new IOException("not connected");

		try {
			return sendPacket(data, inputStream, outputStream, waitResponse,
					packetTimeout);
		} catch (PacketTimeoutException e) {
			throw e;
		} catch (IOException e) {
			synchronized (this) {
				if (s != null)
					s.close();
				s = null;
			}
			throw e;
		}

	}

	@Override
	public synchronized void close() {
		closed = true;
		try {
			if (s != null)
				s.close();
		} catch (IOException e) {
		}
	}

	private Thread thread = new Thread() {
		public void run() {
			while (!closed) {
				try {
					if (s == null) {
						Socket s1 = new Socket(host, port);
						OutputStream outputStream1 = s1.getOutputStream();
						InputStream inputStream1 = s1.getInputStream();
						synchronized (TCPTransceiver.this) {
							s = s1;
							outputStream = outputStream1;
							inputStream = inputStream1;
						}
					}
					sleep(1000);
				} catch (Exception e) {
					// e.printStackTrace();
				}
			}
		}
	};

}
