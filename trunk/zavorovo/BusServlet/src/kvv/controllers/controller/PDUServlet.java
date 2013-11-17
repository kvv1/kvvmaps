package kvv.controllers.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

@SuppressWarnings("serial")
public class PDUServlet extends HttpServlet {

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

		byte[] res = ADUTransceiver.handle(addr, body);

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
