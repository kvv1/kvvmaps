package kvv.mks.view.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class UDPClient {

	private final String host;
	private final int port;

	public UDPClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void send(byte[] data) throws IOException {
		DatagramSocket socket = new DatagramSocket();

		InetAddress address = InetAddress.getByName(host);
		DatagramPacket packet = new DatagramPacket(data, data.length, address,
				port);
		socket.send(packet);
		socket.close();
	}

	public static void main(String[] args) throws IOException {
		UDPServer server = new UDPServer(4445) {
			@Override
			protected void received(DatagramPacket datagramPacket) {
				byte[] data = datagramPacket.getData();
				for (byte b : Arrays.copyOf(data, datagramPacket.getLength()))
					System.out.println(b + " ");
				stop();
			}
		};

		server.start();

		UDPClient client = new UDPClient("localhost", 4445);

		client.send(new byte[] { 0, 1, 2, 3 });

	}

}
