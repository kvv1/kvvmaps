package com.kvv.spot.main.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.kvv.spot.main.shared.SpotRemoveMethod;

public class ImageEditor extends Composite {

	private int mouseX;
	private int mouseY;
	private boolean mousePressed;

	private AbsolutePanel p = new AbsolutePanel();
	private ImageCanvas img;

	private SelCanvas sel;

	public ImageEditor() {
		initWidget(p);
	}

	public void load(String URL) {
		if (img != null)
			p.remove(img);

		img = new ImageCanvas(URL, p, new LoadH() {
			public void onLoad(ImageElement img) {
				p.setSize(img.getWidth() + "px", img.getHeight() + "px");
			}
		});
		p.add(img, 0, 0);

		img.addMouseDownHandler(new MouseDownHandler() {

			@Override
			public void onMouseDown(MouseDownEvent event) {
				mouseX = event.getX();
				mouseY = event.getY();
				mousePressed = true;
				Event.setCapture(img.getElement());
			}
		});
		img.addMouseUpHandler(new MouseUpHandler() {
			@Override
			public void onMouseUp(MouseUpEvent event) {
				mousePressed = false;
				Event.releaseCapture(img.getElement());
			}
		});
		img.addMouseMoveHandler(new MouseMoveHandler() {
			@Override
			public void onMouseMove(MouseMoveEvent event) {
				if (mousePressed && method == null) {
					createSel(mouseX, mouseY, event.getX() - mouseX,
							event.getY() - mouseY);
				}
			}
		});
	}

	private void createSel(int x, int y, int w, int h) {
		if (sel != null)
			p.remove(sel);
		sel = new SelCanvas(x, y, w, h);
		p.add(sel, sel.x, sel.y);
	}

	public void tryRemoveSpot(SpotRemoveMethod method) {
		this.method = method;
		removeSpot(method, false);
	}

	private SpotRemoveMethod method;

	private void removeSpot(SpotRemoveMethod method, boolean save) {
		if (sel != null) {
			if (save)
				img.draw(sel.getCanvasElement(), sel.x, sel.y);
			sel.draw(GWT.getModuleBaseURL() + "spot?x=" + sel.x + "&y=" + sel.y
					+ "&w=" + sel.w + "&h=" + sel.h + "&method=" + method
					+ "&save=" + save);
		}
	}

	public void cancel() {
		method = null;
		if (sel != null) {
			createSel(sel.x, sel.y, sel.w, sel.h);
		}
	}

	public void confirm() {
		if (method != null)
			removeSpot(method, true);
		if (sel != null) {
			p.remove(sel);
			sel = null;
		}
		method = null;
	}
}
