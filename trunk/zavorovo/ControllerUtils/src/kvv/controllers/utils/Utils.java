package kvv.controllers.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utils {
	static public byte fletchSum(byte[] buf, int offset, int len) {
		int S = 0;
		for (; len > 0; len--) {
			byte b = buf[offset++];
			int R = b & 0xFF;
			S += R;
			S = S & 0xFF;
			if (S < R)
				S++;
		}
		// if(S = 255) S = 0;
		return (byte) S;
	}

	public static byte[] send(String url, int addr, byte[] bytes)
			throws IOException {
		String url1 = url + "?addr=" + addr + "&body=";
		String sep = "";
		for (byte b : bytes) {
			url1 += sep + ((int) b & 255);
			sep = ",";
		}

		// System.out.println(url1);

		HttpURLConnection conn = null;
		conn = (HttpURLConnection) new URL(url1).openConnection();
		conn.setRequestMethod("GET");
		// conn.setDoOutput(true);
		conn.connect();

		String resp = new BufferedReader(new InputStreamReader(
				conn.getInputStream())).readLine();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		for (String s : resp.split(",")) {
			int a = Integer.parseInt(s);
			outputStream.write(a);
		}
		return outputStream.toByteArray();
	}

}
