package kvv.controllers.client.page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.Controllers;
import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.Statistics;
import kvv.controllers.shared.Statistics.AddrStaistics;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class StatisticsPage extends Composite {
	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	private VerticalPanel panel = new VerticalPanel();

	public StatisticsPage() {
		show(false);
		initWidget(panel);
	}

	private void show(boolean clear) {
		panel.clear();
		Button refreshButton = new Button("Обновить");
		panel.add(refreshButton);
		refreshButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				show(false);
			}
		});

		Button clearButton = new Button("Очистить");
		panel.add(clearButton);
		clearButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				show(true);
			}
		});

		controllersService.getStatistics(clear,
				new CallbackAdapter<Statistics>() {
					@Override
					public void onSuccess(Statistics statistics) {
						if(statistics == null)
							return;
						ControllerDescr[] controllerDescrs = Controllers.systemDescr.controllers;
						for (ControllerDescr descr : controllerDescrs)
							if (descr != null && descr.addr != 0)
								if (statistics.controllers
										.containsKey(descr.addr))
									add(statistics.controllers.get(descr.addr),
											descr.addr, descr.name);
					}
				});
	}

	protected void add(AddrStaistics addrStaistics, int addr, String name) {
		Label label = new Label(name + "(" + addr + ")");
		label.addStyleName("bold");
		panel.add(label);
		int sum = addrStaistics.errorCnt + addrStaistics.successCnt;
		if (sum == 0) {
			panel.add(new Label("нет статистики"));
			return;
		}
		int percent = addrStaistics.errorCnt * 100 / (sum);

		HorizontalPanel horizontalPanel = new HorizontalPanel();
		label = new Label("ошибки " + percent + "% (" + addrStaistics.errorCnt
				+ "/" + sum + ")");
		label.setWidth("400px");
		horizontalPanel.add(label);
		horizontalPanel.add(createBar(addrStaistics.errorCnt, sum, 16));
		panel.add(horizontalPanel);

		List<String> texts = new ArrayList<String>(
				addrStaistics.errors.keySet());
		Collections.sort(texts);

		for (String text : texts) {
			horizontalPanel = new HorizontalPanel();
			label = new Label(text);
			label.setWidth("400px");
			horizontalPanel.add(label);
			horizontalPanel.add(createBar(addrStaistics.errors.get(text), sum,
					16));
			panel.add(horizontalPanel);
		}

	}

	private Widget createBar(int fill, int sum, int height) {
		Canvas canvas = Canvas.createIfSupported();
		if (sum > 800) {
			fill = fill * 800 / sum;
			sum = 800;
		}

		int width = sum;
		canvas.setPixelSize(width, height);
		canvas.setCoordinateSpaceWidth(width);
		canvas.setCoordinateSpaceHeight(height);

		Context2d context = canvas.getContext2d();
		context.setFillStyle("#C0C0C0");
		context.fillRect(0, 0, width, height);

		context.setFillStyle("#FF0000");
		context.fillRect(0, 0, fill, height);

		context.stroke();
		context.closePath();
		return canvas;
	}

}
