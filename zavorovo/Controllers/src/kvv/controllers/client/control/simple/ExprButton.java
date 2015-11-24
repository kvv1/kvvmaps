package kvv.controllers.client.control.simple;

import kvv.controllers.client.ScheduleService;
import kvv.controllers.client.ScheduleServiceAsync;
import kvv.controllers.client.page.ModePage;
import kvv.controllers.shared.RegisterDescr;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.RegisterSchedule.Expr;
import kvv.gwtutils.client.CallbackAdapter;
import kvv.gwtutils.client.DetPanel;
import kvv.gwtutils.client.form.EditablePanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public abstract class ExprButton extends Composite {
	private final ScheduleServiceAsync scheduleService = GWT
			.create(ScheduleService.class);

	private final VerticalPanel details = new VerticalPanel();
	private final HorizontalPanel savePanel = new HorizontalPanel();
	private final Button save = new Button("Сохранить");

	private final CheckBox localCB = new CheckBox("Локально");

	private final DetPanel<Label, VerticalPanel> detailsPanel;

	private final EditablePanel<ExprContol> expressions = new EditablePanel<ExprContol>(
			!ModePage.controlMode, "Добавить выражение") {
		@Override
		protected ExprContol createItem() {
			return new ExprContol1("", null);
		}

		protected void addAddButton(Button button) {
			savePanel.add(button);
		}

		protected void addNavPanel(ExprContol widget,
				EditablePanel<ExprContol>.NavPanel navPanel) {
			widget.buttonsPanel.add(navPanel);
		};
	};

	private RegisterSchedule registerSchedule;

	private final RegisterDescr reg;

	public ExprButton(RegisterDescr reg) {
		this.reg = reg;

		save.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (registerSchedule == null)
					return;
				registerSchedule.expressions.clear();
				for (ExprContol exprContol : expressions.getItems()) {
					if (!exprContol.getText().trim().isEmpty())
						registerSchedule.expressions.add(new Expr(exprContol
								.getText()));
				}
				registerSchedule.localExpr = localCB.getValue();
				save(registerSchedule);
			}
		});

		localCB.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!ModePage.check()) {
					localCB.setValue(!localCB.getValue());
					return;
				}
			}
		});

		details.add(localCB);
		details.add(expressions);
		if (ModePage.controlMode) {
			savePanel.add(save);
			details.add(savePanel);
		}

		detailsPanel = new DetPanel<Label, VerticalPanel>("Формула", details);

		initWidget(detailsPanel);
	}

	public void updateUI(RegisterSchedule registerSchedule) {
		this.registerSchedule = registerSchedule;

		localCB.setValue(registerSchedule.localExpr);

		boolean err = false;

		expressions.clear();
		for (Expr e : registerSchedule.expressions) {
			expressions.add(new ExprContol1(e.expr, e.errMsg));
			err |= (e.errMsg != null);
		}

		detailsPanel.label.getElement().getStyle()
				.setColor(err ? "red" : "black");

	}

	class ExprContol1 extends ExprContol {
		public ExprContol1(String expr2, String errMsg2) {
			super(expr2, errMsg2);
		}

		@Override
		protected void testExpr(String e) {
			Integer addr = localCB.getValue() ? reg.controllerAddr : null;
			scheduleService.eval(addr, e, new CallbackAdapter<Short>() {
				@Override
				public void onSuccess(Short result) {
					Window.alert("" + result);
				}
			});
		}
	}

	public abstract void save(RegisterSchedule registerSchedule);
}
