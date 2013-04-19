package kvv.controllers.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kvv.controllers.rs485.Rs485;

@SuppressWarnings("serial")
public class ControllerServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		resp.setContentType("text/html");
		int addr = Integer.parseInt(req.getParameter("addr"));
		String sbody = req.getParameter("body");
		String[] body1 = sbody.split(",");
		byte[] body = new byte[body1.length];
		for (int i = 0; i < body1.length; i++) {
			body[i] = (byte) Integer.parseInt(body1[i]);
		}

		byte[] res;
		try {
			res = Rs485.getInstance().send(addr, body);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e);
		}

		String sep = "";
		PrintWriter wr = resp.getWriter();
		for (byte b : res) {
			wr.print(sep + (b & 0xFF));
			sep = ",";
		}
		wr.close();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}
}
