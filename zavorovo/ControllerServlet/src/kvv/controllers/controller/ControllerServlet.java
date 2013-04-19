package kvv.controllers.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kvv.controllers.rs485.Rs485;
import kvv.controllers.utils.Constants;
import kvv.controllers.utils.ErrorCode;
import kvv.controllers.utils.Props;

@SuppressWarnings("serial")
public class ControllerServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String com = Props.getProp(Constants.propsFile, "COM");
		try {
			if (Rs485.instance == null && com != null)
				Rs485.instance = new Rs485(com);
		} catch (Exception e) {
			e.printStackTrace();
		}

		resp.setContentType("text/html");
		int addr = Integer.parseInt(req.getParameter("addr"));
		String sbody = req.getParameter("body");
		String[] body1 = sbody.split(",");
		byte[] body = new byte[body1.length];
		for (int i = 0; i < body1.length; i++) {
			body[i] = (byte) Integer.parseInt(body1[i]);
		}

		try {
			byte[] res = Rs485.instance.send(addr, body);

			String sep = "";
			PrintWriter wr = resp.getWriter();
			System.out.print("response: ");
			for (byte b : res) {
				System.out.print(" " + b);
				wr.print(sep + (b & 0xFF));
				sep = ",";
			}
			System.out.println();
			wr.close();
		} catch (Exception e) {
			e.printStackTrace();
			PrintWriter wr = resp.getWriter();
			wr.print(ErrorCode.ERR_NO_RESPONSE.ordinal());
			wr.close();
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}
}
