package com.kvv.spot.main.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.HasMouseDownHandlers;
import com.google.gwt.event.dom.client.HasMouseMoveHandlers;
import com.google.gwt.event.dom.client.HasMouseUpHandlers;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;

public class ImageCanvas extends Composite implements HasMouseDownHandlers,
		HasMouseUpHandlers, HasMouseMoveHandlers {

	private final Canvas widget;

	public ImageCanvas(String URL, final Panel p, final LoadH loadHandler) {
		widget = Canvas.createIfSupported();
		initWidget(widget);

		final Image img = new Image(URL);
		img.addLoadHandler(new LoadHandler() {
			@Override
			public void onLoad(LoadEvent event) {
				p.remove(img);
				ImageElement ie = ImageElement.as(img.getElement());
				loadHandler.onLoad(ie);
				onLoaded(ie);
			}
		});
		p.add(img);
	}

	private void onLoaded(ImageElement img) {
		int w = img.getWidth();
		int h = img.getHeight();
		widget.setSize(w + "px", h + "px");
		widget.setCoordinateSpaceWidth(w);
		widget.setCoordinateSpaceHeight(h);
		Context2d context = widget.getContext2d();
		context.drawImage(img, 0, 0);
	}

	public void draw(CanvasElement img, int x, int y) {
		Context2d context = widget.getContext2d();
		context.drawImage(img, x, y);
	}

	@Override
	public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
		return widget.addMouseDownHandler(handler);
	}

	@Override
	public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
		return widget.addMouseMoveHandler(handler);
	}

	@Override
	public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
		return widget.addMouseUpHandler(handler);
	}
}
