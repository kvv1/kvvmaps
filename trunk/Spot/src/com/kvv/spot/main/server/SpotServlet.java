package com.kvv.spot.main.server;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.kvv.spot.main.server.spot.DocImpl;
import com.kvv.spot.main.server.spot.SpotRem;
import com.kvv.spot.main.shared.SpotRemoveMethod;

public class SpotServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		SpotRemoveMethod method = SpotRemoveMethod.valueOf(req
				.getParameter("method"));
		boolean save = Boolean.valueOf(req.getParameter("save"));

		int x = Integer.parseInt(req.getParameter("x"));
		int y = Integer.parseInt(req.getParameter("y"));
		int w = Integer.parseInt(req.getParameter("w"));
		int h = Integer.parseInt(req.getParameter("h"));
		double zoom = (Double) req.getSession().getAttribute("zoom");

		if (zoom != 1) {
			x = (int) (x / zoom);
			y = (int) (y / zoom);
			w = (int) (w / zoom);
			h = (int) (h / zoom);
		}

		BufferedImage img = (BufferedImage) req.getSession().getAttribute(
				"image");

		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;
		if (w < 4)
			w = 4;
		if (h < 4)
			h = 4;
		if (x >= img.getWidth())
			x = img.getWidth() - 4;
		if (y >= img.getHeight())
			y = img.getHeight() - 4;
		if (x + w > img.getWidth())
			w = img.getWidth() - x;
		if (y + h > img.getHeight())
			h = img.getHeight() - y;

		BufferedImage img1 = removeSpot(img, x, y, w, h, method, save);

		UploadImageServlet.returnImage(resp, img1, zoom);

	}

	private BufferedImage removeSpot(BufferedImage img, int x, int y, int w,
			int h, SpotRemoveMethod method, boolean save) {

		BufferedImage img1 = new BufferedImage(w, h, img.getType());
		Graphics2D g1 = (Graphics2D) img1.getGraphics();

		g1.drawImage(img, 0, 0, w, h, x, y, x + w, y + h, new ImageObserver() {
			@Override
			public boolean imageUpdate(Image img, int infoflags, int x, int y,
					int width, int height) {
				return (infoflags & ALLBITS) != 0;
			}
		});

		g1.dispose();

		removeSpot(img1, img.getWidth() / 150, method);

		if (save) {
			Graphics2D g = (Graphics2D) img.getGraphics();
			g.drawImage(img1, x, y, x + w, y + h, 0, 0, w, h,
					new ImageObserver() {
						@Override
						public boolean imageUpdate(Image img, int infoflags,
								int x, int y, int width, int height) {
							return (infoflags & ALLBITS) != 0;
						}
					});
			g.dispose();
		}

		return img1;
	}

	private void removeSpot(final BufferedImage img, int diam,
			SpotRemoveMethod method) {
		DocImpl doc = new DocImpl() {
			@Override
			public int getPixel(int x, int y) {
				return img.getRGB(x, y);
			}

			@Override
			public int getWidth() {
				return img.getWidth();
			}

			@Override
			public int getHeight() {
				return img.getHeight();
			}

			@Override
			public void setPixel(int x, int y, int pixel) {
				img.setRGB(x, y, pixel);
			}
		};

		boolean horiz = (method == SpotRemoveMethod.VH || method == SpotRemoveMethod.H);
		boolean vert = (method == SpotRemoveMethod.VH || method == SpotRemoveMethod.V);

		try {
			SpotRem.remove(doc, diam, horiz, vert);
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

}
