package kvv.controllers.client.page;

import java.util.Date;

import kvv.controllers.client.Controllers;
import kvv.controllers.client.control.ControlCompositeWithDiagrams;
import kvv.controllers.client.control.simple.AutoRelayControl;
import kvv.controllers.client.control.simple.ControllerUIForm;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.ControllerType;
import kvv.controllers.shared.RegisterDescr;
import kvv.controllers.shared.RegisterPresentation;
import kvv.controllers.shared.UnitDescr;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class UnitPage extends ControlCompositeWithDiagrams {
	private final RadioButton historyOff = new RadioButton("history"
			+ hashCode(), "Выкл");
	private final RadioButton historyToday = new RadioButton("history"
			+ hashCode(), "Сегодня");
	private final RadioButton historyYesterday = new RadioButton("history"
			+ hashCode(), "Вчера");

	private final UnitDescr unit;

	public UnitPage(final UnitDescr unit) {
		this.unit = unit;

		Button refreshButton = new Button("Обновить");
		refreshButton.setWidth("100%");
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
				ControllerDescr descr = null;
				for (ControllerDescr d : Controllers.systemDescr.controllers)
					if (d.name.equals(controllerName))
						descr = d;
				ControllerType controllerType = null;
				if (descr != null)
					controllerType = Controllers.systemDescr.controllerTypes
							.get(descr.type);
				if (controllerType != null) {
					ControllerUIForm form = new ControllerUIForm(descr.addr, controllerName,
							controllerType.def, false);
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
