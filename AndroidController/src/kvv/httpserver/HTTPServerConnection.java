package kvv.httpserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.StringTokenizer;

class HTTPServerConnection extends Thread {
	static final String HTML_START = "<html>"
			+ "<title>HTTP Server in java</title>" + "<body>";

	static final String HTML_END = "</body>" + "</html>";

	private final HTTPServer server;
	private final Socket clientSocket;
	private BufferedReader inFromClient = null;
	private DataOutputStream outToClient = null;

	public HTTPServerConnection(HTTPServer server, Socket clientSocket) {
		this.server = server;
		this.clientSocket = clientSocket;
		setDaemon(true);
	}

	@Override
	public void run() {

		try {

			System.out.println("The Client " + clientSocket.getInetAddress()
					+ ":" + clientSocket.getPort() + " is connected");

			inFromClient = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			outToClient = new DataOutputStream(
					clientSocket.getOutputStream());

			String requestString = inFromClient.readLine();
			String headerLine = requestString;

			StringTokenizer tokenizer = new StringTokenizer(headerLine);
			String httpMethod = tokenizer.nextToken();
			String httpQueryString = tokenizer.nextToken();

			System.out.println("The HTTP request string is ....");
			while (inFromClient.ready()) {
				// Read the HTTP complete HTTP Query
				System.out.println(requestString);
				requestString = inFromClient.readLine();
			}

			if (httpMethod.equals("GET")) {
				String resp = server.handleRequest(httpQueryString);
				if (resp != null) {
					// The default home page
					sendResponse(200, resp);
				} else {
					sendResponse(
							404,
							"<b>The Requested resource not found ...."
									+ "Usage: http://127.0.0.1:5000 or http://127.0.0.1:5000/</b>");
				}
			} else
				sendResponse(
						404,
						"<b>The Requested resource not found ...."
								+ "Usage: http://127.0.0.1:5000 or http://127.0.0.1:5000/</b>");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendResponse(int statusCode, String responseString)
			throws Exception {

		String statusLine = null;
		String serverdetails = "Server: Java HTTPServer";
		String contentLengthLine = null;
		String contentTypeLine = "Content-Type: text/html" + "\r\n";

		if (statusCode == 200)
			statusLine = "HTTP/1.1 200 OK" + "\r\n";
		else
			statusLine = "HTTP/1.1 404 Not Found" + "\r\n";

		// responseString = HTTPServer.HTML_START + responseString
		// + HTTPServer.HTML_END;
		contentLengthLine = "Content-Length: " + responseString.length()
				+ "\r\n";

		outToClient.writeBytes(statusLine);
		outToClient.writeBytes(serverdetails);
		outToClient.writeBytes(contentTypeLine);
		outToClient.writeBytes(contentLengthLine);
		outToClient.writeBytes("Connection: close\r\n");
		outToClient.writeBytes("\r\n");

		outToClient.writeBytes(responseString);

		outToClient.close();
	}

}
