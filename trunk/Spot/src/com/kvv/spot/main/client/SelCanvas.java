package com.kvv.spot.main.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;

public class SelCanvas extends Composite {
	private Canvas canvas = Canvas.createIfSupported();
	public final int x;
	public final int y;
	public final int w;
	public final int h;

	public SelCanvas(int x, int y, int w, int h) {
		if (w < 0)
			w = 0;
		if (h < 0)
			h = 0;
		if (w < 4)
			w = 4;
		if (h < 4)
			h = 4;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		
		canvas.setSize(w + "px", h + "px");
		canvas.setCoordinateSpaceWidth(w);
		canvas.setCoordinateSpaceHeight(h);

		Context2d context = canvas.getContext2d();
		context.beginPath();
		context.rect(1, 1, w - 2, h - 2);
		context.closePath();
		context.stroke();

		initWidget(canvas);
	}

	public void draw(ImageElement img) {
		canvas.getContext2d().drawImage(img, 0, 0);
	}

	public void draw(String url) {
		final Image image = new Image(url);

		Timer t = new Timer() {
			public void run() {
				if (image.getWidth() > 0) {
					draw(ImageElement.as(image.getElement()));
					cancel();
				}
			}
		};
		t.scheduleRepeating(100);
	}

	public CanvasElement getCanvasElement() {
		return (CanvasElement) CanvasElement.as(this.getElement());
	}

}
