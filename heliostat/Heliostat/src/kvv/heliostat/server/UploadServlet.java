package kvv.heliostat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kvv.heliostat.server.envir.Envir;
import kvv.heliostat.server.envir.RealEnvir;
import kvv.heliostat.server.envir.controller.IController;

@SuppressWarnings("serial")
public class UploadServlet extends HttpServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter wr = new PrintWriter(response.getOutputStream());
		try {
			int addr = Integer.parseInt(request.getParameter("addr"));
			InputStream is = request.getInputStream();
			IController controller = ((RealEnvir) (Envir.instance)).controller;
			controller.uploadApp(addr, getImageHex(is));
			is.close();
			wr.print("Uploading complete");
		} catch (Exception e) {
			wr.print("Error uploading: " + e.getClass().getName() + " "
					+ e.getMessage());
		}
		wr.close();
	}

	private byte[] getImageHex(InputStream is) throws IOException {
		List<Byte> bytes = new ArrayList<Byte>();

		int addr = 0;

		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line = rd.readLine()) != null) {
			if (line.length() == 0 || line.charAt(0) != ':')
				continue;
			int idx = 1;
			int byteCnt = Integer.parseInt(line.substring(idx, idx + 2), 16);
			idx += 2;
			int a = Integer.parseInt(line.substring(idx, idx + 4), 16);
			idx += 4;
			int cmd = Integer.parseInt(line.substring(idx, idx + 2), 16);
			idx += 2;

			if (cmd == 0) {
				if (a != addr)
					throw new IllegalArgumentException();

				for (int i = 0; i < byteCnt; i++) {
					int b = Integer.parseInt(line.substring(idx, idx + 2), 16);
					idx += 2;
					bytes.add((byte) b);
				}

				addr += byteCnt;
			}
		}
		System.out.println();
		rd.close();

		byte[] res = new byte[bytes.size()];
		int i = 0;
		for (Byte b : bytes)
			res[i++] = b;

		return res;
	}

}
