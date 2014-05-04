package kvv.controllers.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

@SuppressWarnings("serial")
public class StatisticsServlet extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		boolean clear = Boolean.valueOf(req.getParameter("clear"));
		if (clear) {
			ADUTransceiver.statistics.clear();
			return;
		}

		resp.setContentType("text/html");
		PrintWriter wr = resp.getWriter();

		synchronized (ADUTransceiver.statistics) {
			wr.print(new Gson().toJson(ADUTransceiver.statistics));
		}

		wr.close();
	}

}
