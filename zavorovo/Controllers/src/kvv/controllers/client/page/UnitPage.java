package kvv.controllers.client.page;

import java.util.Date;

import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.control.ControlCompositeWithDiagrams;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.PageDescr;
import kvv.controllers.shared.Register;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class UnitPage extends ControlCompositeWithDiagrams {
	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);
	private final RadioButton historyOff = new RadioButton("history"
			+ hashCode(), "Выкл");
	private final RadioButton historyToday = new RadioButton("history"
			+ hashCode(), "Сегодня");
	private final RadioButton historyYesterday = new RadioButton("history"
			+ hashCode(), "Вчера");

	public UnitPage(final PageDescr page) {
		Button refreshButton = new Button("Обновить");
		refreshButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refresh();
			}
		});

		VerticalPanel panel = new VerticalPanel();
		panel.add(refreshButton);

		if (page.controllers != null) {
			HorizontalPanel controllersPanel1 = new HorizontalPanel();
			controllersPanel1.setSpacing(4);
			controllersPanel1.setBorderWidth(1);
			for (String controllerName : page.controllers) {
				int addr = -1;
				for (ControllerDescr descr : ControllersPage.controllers)
					if (descr.name.equals(controllerName))
						addr = descr.addr;
				if (addr >= 0) {
					Form form = new Form(addr, controllerName, false);
					controllersPanel1.add(form);
					add(form);
				} else {
					Window.alert("Неизвестное имя контроллера '"
							+ controllerName + "'");
				}
			}
			panel.add(controllersPanel1);
		}

		if (page.registers != null && page.registers.length > 0) {
			ClickHandler historyClickHandler = new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					refresh();
				}
			};

			historyOff.addClickHandler(historyClickHandler);
			historyToday.addClickHandler(historyClickHandler);
			historyYesterday.addClickHandler(historyClickHandler);

			HorizontalPanel historyRadioPanel = new HorizontalPanel();
			historyRadioPanel.add(historyOff);
			historyRadioPanel.add(historyToday);
			historyRadioPanel.add(historyYesterday);

			panel.add(new Label("Показывать историю:"));
			panel.add(historyRadioPanel);

			historyOff.setValue(true);

			for (String regName : page.registers) {
				l1: for (ControllerDescr descr : ControllersPage.controllers)
					if (descr != null && descr.registers != null)
						for (Register register : descr.registers)
							if (register != null
									&& register.name.equals(regName)) {
								AutoRelayControl autoRelayControl = new AutoRelayControl(
										register, mouseMoveHandler);
								panel.add(autoRelayControl);
								add(autoRelayControl);
								diagrams.add(autoRelayControl);
								break l1;
							}
			}
		}

		TextWithSaveButton script = new TextWithSaveButton("", "100%", "400px") {
			@Override
			protected void save(final String text,
					final AsyncCallback<Void> callback) {
				controllersService.savePageScript(page.name, text,
						new AsyncCallback<Void>() {

							@Override
							public void onFailure(Throwable caught) {
								callback.onFailure(caught);
							}

							@Override
							public void onSuccess(Void result) {
								page.script = text;
								callback.onSuccess(result);
							}
						});
			}
		};

		script.setText(page.script);

		script.setWidth("100%");
		panel.add(script);

		initWidget(panel);
		refresh();
	}

	@Override
	public void refresh() {
		super.refresh();
		refreshDiagrams();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Date getDateForHistory(Date now) {
		if (historyToday.getValue())
			return now;
		if (historyYesterday.getValue()) {
			Date d = new Date(now.getYear(), now.getMonth(), now.getDate());
			return new Date(d.getTime() - 60000); // any time yesterday
		}
		return null;
	}

}
