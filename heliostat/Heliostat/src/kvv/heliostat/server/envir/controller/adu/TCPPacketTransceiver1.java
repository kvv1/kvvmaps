package kvv.heliostat.server.envir.controller.adu;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TCPPacketTransceiver1 implements IPacketTransceiver {
	private static final long BYTE_TIMEOUT = 50;
	private final int packetTimeout;
	private final int port;
	private final String host;

	private volatile Socket s;
	private InputStream inputStream;
	private OutputStream outputStream;

	public TCPPacketTransceiver1(String host, int port, int packetTimeout) {
		this.packetTimeout = packetTimeout;
		this.host = host;
		this.port = port;
		thread.start();
	}

	@Override
	public synchronized byte[] sendPacket(byte[] data, boolean waitResponse)
			throws IOException {
		if (s == null)
			throw new IOException("not connected");

		try {
			while (inputStream.available() > 0)
				inputStream.read();
			outputStream.write(data);
			outputStream.flush();

			long timeout = packetTimeout;

			List<Byte> buffer = new ArrayList<>();

			long t = System.currentTimeMillis();

			for (;;) {
				while (System.currentTimeMillis() - t < timeout
						&& inputStream.available() == 0) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e1) {
					}
				}
				if (inputStream.available() == 0) {
					if (buffer.isEmpty())
						throw new PacketTimeoutException();
					byte[] res = new byte[buffer.size()];
					for (int i = 0; i < buffer.size(); i++) {
						res[i] = buffer.get(i);
					}
					return res;
				} else {
					while (inputStream.available() > 0)
						buffer.add((byte) inputStream.read());
					timeout = BYTE_TIMEOUT;
					t = System.currentTimeMillis();
				}
			}
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

	private volatile boolean stopped;

	private Thread thread = new Thread() {
		public void run() {
			while (!stopped) {
				try {
					if (s == null) {
						// Socket s1 = new Socket();
						// s1.connect(new InetSocketAddress(host, port),1000);

						Socket s1 = new Socket(host, port);
						OutputStream outputStream1 = s1.getOutputStream();
						InputStream inputStream1 = s1.getInputStream();
						synchronized (TCPPacketTransceiver1.this) {
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

	@Override
	public synchronized void close() {
		stopped = true;
		try {
			if (s != null)
				s.close();
		} catch (IOException e) {
		}
	}

}
