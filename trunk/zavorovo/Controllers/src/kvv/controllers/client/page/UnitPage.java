package kvv.controllers.client.page;

import java.util.Date;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.Controllers;
import kvv.controllers.client.UnitService;
import kvv.controllers.client.UnitServiceAsync;
import kvv.controllers.client.control.ControlCompositeWithDiagrams;
import kvv.controllers.client.control.simple.AutoRelayControl;
import kvv.controllers.client.control.simple.Form;
import kvv.controllers.client.control.simple.TextWithSaveButton;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.RegisterDescr;
import kvv.controllers.shared.RegisterPresentation;
import kvv.controllers.shared.ScriptData;
import kvv.controllers.shared.UnitDescr;

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
	private final UnitServiceAsync unitService = GWT.create(UnitService.class);
	private final RadioButton historyOff = new RadioButton("history"
			+ hashCode(), "Выкл");
	private final RadioButton historyToday = new RadioButton("history"
			+ hashCode(), "Сегодня");
	private final RadioButton historyYesterday = new RadioButton("history"
			+ hashCode(), "Вчера");

	private final Label errMsg = new Label();
	private final CheckBox vmCB = new CheckBox();
	private final UnitDescr unit;

	private final TextWithSaveButton script = new TextWithSaveButton("",
			"100%", "400px") {
		@Override
		protected void save(final String text,
				final AsyncCallback<Void> callback) {
			unitService.savePageScript(unit.name, text,
					new AsyncCallback<Void>() {

						@Override
						public void onFailure(Throwable caught) {
							callback.onFailure(caught);
							refreshScript();
						}

						@Override
						public void onSuccess(Void result) {
							callback.onSuccess(result);
							refreshScript();
						}
					});
		}
	};

	public UnitPage(final UnitDescr unit) {
		this.unit = unit;

		Button refreshButton = new Button("Обновить");
		refreshButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refresh();
			}
		});

		VerticalPanel panel = new VerticalPanel();
		panel.add(refreshButton);

		if (unit.controllers != null) {
			HorizontalPanel controllersPanel1 = new HorizontalPanel();
			controllersPanel1.setSpacing(4);
			controllersPanel1.setBorderWidth(1);
			for (String controllerName : unit.controllers) {
				int addr = -1;
				for (ControllerDescr descr : Controllers.systemDescr.controllers)
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

		if (unit.registers != null && unit.registers.length > 0) {
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

			for (RegisterPresentation regPres : unit.registers) {
				l1: for (ControllerDescr descr : Controllers.systemDescr.controllers)
					if (descr != null && descr.registers != null)
						for (RegisterDescr register : descr.registers)
							if (register != null
									&& register.name.equals(regPres.name)) {
								AutoRelayControl autoRelayControl = new AutoRelayControl(
										register, regPres, mouseMoveHandler);
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
				unitService.enableScript(unit.name, vmCB.getValue(),
						new CallbackAdapter<Void>() {
							@Override
							public void onSuccess(Void result) {
								refreshScript();
							}
							@Override
							public void onFailure(Throwable caught) {
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
		unitService.getScriptData(unit.name, new CallbackAdapter<ScriptData>() {
			@Override
			public void onSuccess(ScriptData result) {
				script.setText(result.text);
				errMsg.setText(result.err);
				vmCB.setValue(result.enabled);
			}

			@Override
			public void onFailure(Throwable caught) {
			}
		});
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
