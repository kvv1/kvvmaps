package kvv.mks.view.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public abstract class UDPServer {

	private DatagramSocket socket;
	private volatile boolean stopped;

	protected abstract void received(DatagramPacket datagramPacket);
	
	private final Thread thread = new Thread("UDPServer") {
		@Override
		public void run() {
			while (!stopped) {
				try {
					byte[] buf = new byte[256];
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					socket.receive(packet);
					received(packet);
				} catch (IOException e) {
				}
			}
		}
	};
	
	public UDPServer(int port) throws SocketException {
		socket = new DatagramSocket(port);
	}

	public void start() {
		thread.start();
	}
	
	public void stop() {
		stopped = true;
		socket.close();
 		thread.interrupt();
	}
}
