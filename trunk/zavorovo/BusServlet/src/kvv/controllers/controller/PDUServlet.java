package kvv.controllers.controller;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kvv.controllers.rs485.PacketTransceiver;
import kvv.controllers.utils.ADU;
import kvv.controllers.utils.PDU;

import com.google.gson.Gson;

@SuppressWarnings("serial")
public class PDUServlet extends HttpServlet {

	private static Set<Integer> failedAddrs = new HashSet<Integer>();

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
		PDU pdu = new PDU(body);
		ADU adu = new ADU(addr, pdu);

		synchronized (failedAddrs) {
			int attempts = failedAddrs.contains(addr) ? 1 : 3;
			failedAddrs.remove(addr);
			for (int i = 0; i < attempts; i++) {
				byte[] res = null;
				try {
					res = PacketTransceiver.getInstance().sendPacket(
							adu.toBytes(), addr != 0);
					if (res == null)
						break;

					ADU adu1 = ADU.fromBytes(res);
					if (adu1 == null)
						throw new IOException("wrong response ADU checksum");
					if (adu1.addr != addr)
						throw new IOException("wrong response ADU addr");

					PrintWriter wr = resp.getWriter();
					wr.print(new Gson().toJson(adu1.pdu.toBytes()));
					wr.close();
					break;
				} catch (Exception e) {
					if (i < attempts - 1)
						continue;
					failedAddrs.add(addr);
					String msg = "cmd: ";
					for (byte b : body)
						msg += Integer.toHexString((int) b & 0xFF) + " ";
					msg += ", response: ";
					if (res == null)
						msg += "null";
					else
						for (byte b : res)
							msg += Integer.toHexString((int) b & 0xFF) + " ";
					BusLogger.logErr(addr, msg + " " + e.getMessage());

					try {
						PrintStream ps = new PrintStream(new FileOutputStream(
								"c:/zavorovo/log/l.txt", true), true,
								"Windows-1251");
						e.printStackTrace(ps);
						ps.close();
					} catch (Exception e1) {
					}

					return;
				}
			}
			BusLogger.logSuccess(addr);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}
}
