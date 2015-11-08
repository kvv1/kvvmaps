package kvv.controllers.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kvv.controllers.server.context.Context;
import kvv.stdutils.Utils;

@SuppressWarnings("serial")
public class UploadServlet extends HttpServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter wr = new PrintWriter(response.getOutputStream());
		try {
			int addr = Integer.parseInt(request.getParameter("addr"));
			InputStream is = request.getInputStream();
			Context.getInstance().controller.uploadApp(addr, Utils.getImageHex(is));
			is.close();
			wr.print("Uploaded");
		} catch (Exception e) {
			wr.print("Upload error: " + e.getClass().getName() + " " + e.getMessage());
		}
		wr.close();
	}

}
