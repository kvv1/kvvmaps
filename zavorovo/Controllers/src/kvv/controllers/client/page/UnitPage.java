package kvv.controllers.client.page;

import java.util.Date;
import java.util.Map;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.Controllers;
import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.control.ControlCompositeWithDiagrams;
import kvv.controllers.client.control.simple.AutoRelayControl;
import kvv.controllers.client.control.simple.Form;
import kvv.controllers.client.control.simple.TextWithSaveButton;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.UnitDescr;
import kvv.controllers.shared.Register;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
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

	private final Label errMsg = new Label();
	private final CheckBox vmCB = new CheckBox();
	private final UnitDescr page;

	private final TextWithSaveButton script = new TextWithSaveButton("",
			"100%", "400px") {
		@Override
		protected void save(final String text,
				final AsyncCallback<Void> callback) {
			controllersService.savePageScript(page.name, text,
					new AsyncCallback<Void>() {

						@Override
						public void onFailure(Throwable caught) {
							callback.onFailure(caught);
							refreshScript();
						}

						@Override
						public void onSuccess(Void result) {
							page.script = text;
							callback.onSuccess(result);
							refreshScript();
						}
					});
		}
	};

	public UnitPage(final UnitDescr page) {
		this.page = page;

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
				for (ControllerDescr descr : Controllers.systemDescr.controllerDescrs)
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
				l1: for (ControllerDescr descr : Controllers.systemDescr.controllerDescrs)
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

		vmCB.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				controllersService.enableScript(page.name, vmCB.getValue(),
						new CallbackAdapter<Void>() {
							@Override
							public void onSuccess(Void result) {
								refreshScript();
							}
						});
			}
		});

		panel.add(vmCB);
		panel.add(errMsg);

		script.setWidth("100%");
		panel.add(script);

		initWidget(panel);
		refresh();
	}

	@Override
	public void refresh() {
		super.refresh();
		refreshDiagrams();
		refreshScript();
	}

	private void refreshScript() {
		controllersService
				.getVMErrors(new CallbackAdapter<Map<String, String>>() {
					@Override
					public void onSuccess(Map<String, String> result) {
						errMsg.setText(result.get(page.name));
					}
				});
		for (UnitDescr page : Controllers.systemDescr.unitDescrs)
			if (page.name.equals(UnitPage.this.page.name)) {
				script.setText(page.script);
				vmCB.setValue(page.scriptEnabled);
			}
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
