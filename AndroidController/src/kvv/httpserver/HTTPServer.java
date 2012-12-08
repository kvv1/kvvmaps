package kvv.httpserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class HTTPServer extends Thread {
	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			// Log.e(LOG_TAG, ex.toString());
		}
		return null;
	}

	private ServerSocket serverSocket;

	public void close() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			Socket connected;
			try {
				connected = serverSocket.accept();
				(new HTTPServerConnection(this, connected)).start();
			} catch (IOException e) {
				if (interrupted())
					return;
				e.printStackTrace();
				try {
					sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	@Override
	public synchronized void start() {
		this.stop();
		int port = 8080;
		try {
			serverSocket = new ServerSocket(port, 10/*
													 * , InetAddress.getByName(
													 * getLocalIpAddress())
													 */);
			System.out.println("TCPServer Waiting for client on port " + port);
			super.start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<HTTPHandler> handlers = new ArrayList<HTTPHandler>();
	
	public void addHandler(HTTPHandler handler) {
		handlers.add(handler);
	}

	public String handleRequest(String httpQueryString) {
		for (HTTPHandler handler : handlers) {
			if (httpQueryString.equals(handler.path)
					|| httpQueryString.startsWith(handler.path + "/")
					|| httpQueryString.startsWith(handler.path + "?")) {
				return handler.handle(httpQueryString);
			}
		}
		return null;
	}
}