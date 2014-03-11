package kvv.controllers.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kvv.controllers.server.context.Context;

@SuppressWarnings("serial")
public class UploadServlet extends HttpServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter wr = new PrintWriter(response.getOutputStream());
		try {
			int addr = Integer.parseInt(request.getParameter("addr"));
			InputStream is = request.getInputStream();
			Context.getInstance().controller.uploadAppHex(addr, is);
			is.close();
			wr.print("Загрузка завершена");
		} catch (Exception e) {
			wr.print("Ошибка при загрузке: " + e.getClass().getName() + " " + e.getMessage());
		}
		wr.close();
	}
}
