package kvv.controllers.controller;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPModbusServer {

	private volatile boolean closed;

	public TCPModbusServer() throws IOException {
		final ServerSocket serverSocket = new ServerSocket(90);

		new Thread("TCPModbus ServerSocket") {
			public void run() {
				while (!closed) {
					try {
						final Socket socket = serverSocket.accept();
						new Thread("TCPModbus ClientSocket") {
							public void run() {
								try {
									handle(socket);
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								try {
									socket.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}.start();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();

	}

	public void close() {
		closed = true;
	}

	private void handle(Socket socket) throws IOException {
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		dis.readShort();
		dis.readShort();
		short len = dis.readShort();
		byte addr = dis.readByte();
		byte[] data = new byte[len - 2];
		for (int i = 0; i < len - 1; i++)
			data[i] = dis.readByte();
		byte[] res = ADUTransceiver.handle(addr, data);
		socket.getOutputStream().write(res);
	}
}
