package kvv.heliostat.server.envir.controller.adu;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TCPPacketTransceiver implements IPacketTransceiver {
	private static final long BYTE_TIMEOUT = 100;
	private final int packetTimeout;
	private final int port;
	private final String host;

	public TCPPacketTransceiver(String host, int port, int packetTimeout) {
		this.packetTimeout = packetTimeout;
		this.host = host;
		this.port = port;
	}

	@Override
	public synchronized byte[] sendPacket(byte[] data, boolean waitResponse)
			throws IOException {
		return __sendPacket(data, waitResponse, packetTimeout);
	}

	private final BlockingQueue<Byte> queue = new LinkedBlockingQueue<>();

	private synchronized byte[] __sendPacket(byte[] data, boolean waitResponse,
			int packetTimeout) throws IOException {

		final Socket s = new Socket(host, port);

		try {

			OutputStream os = s.getOutputStream();
			os.write(data);
			os.flush();
			final InputStream inStream = s.getInputStream();

			queue.clear();

			new Thread() {
				public void run() {
					try {
						while (true) {
							int ch = inStream.read();
							queue.add((byte) ch);
						}
					} catch (IOException e) {
					}
				}
			}.start();

			long timeout = packetTimeout;

			List<Byte> buffer = new ArrayList<>();

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

		} finally {
			if (s != null)
				s.close();

		}
	}

	@Override
	public void close() {
	}

}
