package kvv.controllers.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kvv.controllers.rs485.PacketTransceiver;
import kvv.controllers.utils.ADU;
import kvv.controllers.utils.RTU;

import com.google.gson.Gson;

@SuppressWarnings("serial")
public class RTUServlet extends HttpServlet {

	private Set<Integer> failedAddrs = new HashSet<Integer>();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		resp.setContentType("text/html");
		int addr = Integer.valueOf(req.getParameter("addr"));
		String sbody = req.getParameter("body");
		int[] body1 = new Gson().fromJson(sbody, int[].class);
		byte[] body = new byte[body1.length];
		for (int i = 0; i < body1.length; i++)
			body[i] = (byte) body1[i];
		RTU rtu = new RTU(body);
		ADU adu = new ADU(addr, rtu);

		synchronized (failedAddrs) {
			int attempts = failedAddrs.contains(addr) ? 1 : 3;
			failedAddrs.remove(addr);
			for (int i = 0; i < attempts; i++) {
				byte[] res = null;
				try {
					res = PacketTransceiver.getInstance().sendPacket(
							adu.toBytes(), addr != 0, 1);
					if (res != null) {
						adu = ADU.fromBytes(res);
						if (adu != null) {
							PrintWriter wr = resp.getWriter();
							wr.print(new Gson().toJson(adu.rtu.toBytes()));
							wr.close();
							break;
						}
					}
					break;
				} catch (Exception e) {
				}
				if (i == attempts - 1) {
					failedAddrs.add(addr);
					String msg = "wrong response from addr: " + addr;
					msg += ", cmd: ";
					for (byte b : body)
						msg += Integer.toHexString((int) b & 0xFF) + " ";
					msg += ", response: ";
					for (byte b : res)
						msg += Integer.toHexString((int) b & 0xFF) + " ";
					throw new IOException(msg);
				}
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}
}
