package kvv.controllers.client.page;

import java.util.Date;

import kvv.controllers.client.Controllers;
import kvv.controllers.client.control.ControlCompositeWithDiagrams;
import kvv.controllers.client.control.simple.AutoRelayControl;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.RegisterDescr;
import kvv.controllers.shared.RegisterPresentation;
import kvv.controllers.shared.UnitDescr;
import kvv.gwtutils.client.HorPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
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

	private final RadioButton history2 = new RadioButton(
			"history" + hashCode(), "2");
	private final RadioButton history3 = new RadioButton(
			"history" + hashCode(), "3");
	private final RadioButton history4 = new RadioButton(
			"history" + hashCode(), "4");

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

			panel.add(new Label("Показывать историю:"));
			panel.add(new HorPanel(true, 8, historyOff, historyToday,
					historyYesterday, history2, history3, history4));

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

	private static final DateTimeFormat df = DateTimeFormat.getFormat("dd.MM");

	@SuppressWarnings("deprecation")
	@Override
	protected Date getDateForHistory(Date now) {
		if (historyToday.getValue())
			return now;

		Date d1 = new Date(new Date(now.getYear(), now.getMonth(),
				now.getDate()).getTime() - 60000);
		Date d2 = new Date(new Date(now.getYear(), now.getMonth(),
				now.getDate()).getTime()
				- 1 * (60000 * 60 * 24) - 60000);
		Date d3 = new Date(new Date(now.getYear(), now.getMonth(),
				now.getDate()).getTime()
				- 2 * (60000 * 60 * 24) - 60000);
		Date d4 = new Date(new Date(now.getYear(), now.getMonth(),
				now.getDate()).getTime()
				- 3 * (60000 * 60 * 24) - 60000);

		history2.setText(df.format(d2));
		history3.setText(df.format(d3));
		history4.setText(df.format(d4));

		if (historyYesterday.getValue())
			return d1;

		if (history2.getValue())
			return d2;

		if (history3.getValue())
			return d3;

		if (history4.getValue())
			return d4;

		return null;
	}

}
