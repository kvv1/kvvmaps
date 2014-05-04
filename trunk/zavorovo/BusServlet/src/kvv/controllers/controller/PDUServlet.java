package kvv.controllers.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kvv.controllers.utils.Utils;

import com.google.gson.Gson;

@SuppressWarnings("serial")
public class PDUServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		resp.setContentType("text/html");
		int addr = Integer.parseInt(req.getParameter("addr"));
		Integer timeout = Utils.getPropInt("c:/zavorovo/controller.properties", "timeout");
		if(timeout == null)
			timeout = 600;
		String stimeout = req.getParameter("timeout");
		if (stimeout != null)
			timeout = Integer.parseInt(stimeout);

		String sbody = req.getParameter("body");
		int[] body1 = new Gson().fromJson(sbody, int[].class);
		byte[] body = new byte[body1.length];
		for (int i = 0; i < body1.length; i++)
			body[i] = (byte) body1[i];

		byte[] res = ADUTransceiver.handle(addr, body, timeout);

		PrintWriter wr = resp.getWriter();
		wr.print(new Gson().toJson(res));
		wr.close();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}
}
