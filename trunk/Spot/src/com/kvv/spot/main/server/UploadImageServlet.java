package com.kvv.spot.main.server;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class UploadImageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		InputStream is = req.getInputStream();
		int c;

		int n = 0;
		while ((c = is.read()) != -1) {
			System.out.print((char) c);
			if (c == '\n' && ++n == 4)
				break;
		}

		BufferedImage img = ImageIO.read(is);

		int w = img.getWidth();
		int h = img.getHeight();

		double zoom = 1;
		if (w > 1024)
			zoom = Math.min(zoom, (double)1024 / w);
		if (h > 768)
			zoom = Math.min(zoom, (double)768 / h);

		req.getSession().setAttribute("image", img);
		req.getSession().setAttribute("zoom", zoom);

		System.out.println("POST " + req.getSession());
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		BufferedImage img = (BufferedImage) req.getSession().getAttribute(
				"image");

		System.out.println("GET " + req.getSession());

		returnImage(resp, img, (Double) req.getSession().getAttribute("zoom"));
	}

	public static void returnImage(HttpServletResponse resp, BufferedImage img,
			double zoom) throws IOException {
		resp.setHeader("Cache-Control", "no-store");
		resp.setHeader("Pragma", "no-cache");
		resp.setDateHeader("Expires", 0);
		resp.setContentType("image/jpeg");

		if (zoom < 1) {
			int w = (int) (img.getWidth() * zoom);
			int h = (int) (img.getHeight() * zoom);
			BufferedImage img1 = new BufferedImage(w, h, img.getType());
			Graphics g = img1.getGraphics();
			g.drawImage(img, 0, 0, w, h, new ImageObserver() {
				@Override
				public boolean imageUpdate(Image img, int infoflags, int x,
						int y, int width, int height) {
					return (infoflags & ALLBITS) != 0;
				}
			});
			g.dispose();
			img = img1;
		}

		ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();

		JPEGImageEncoder jpegEncoder = JPEGCodec
				.createJPEGEncoder(jpegOutputStream);

		jpegEncoder.encode(img);

		ServletOutputStream responseOutputStream = resp.getOutputStream();

		responseOutputStream.write(jpegOutputStream.toByteArray());

		responseOutputStream.flush();
		responseOutputStream.close();
	}
}
