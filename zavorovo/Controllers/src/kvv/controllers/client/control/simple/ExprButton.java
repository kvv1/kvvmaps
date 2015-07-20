package kvv.controllers.client.control.simple;

import kvv.controllers.client.page.ModePage;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.RegisterSchedule.Expr;
import kvv.controllers.shared.RegisterSchedule.State;
import kvv.gwtutils.client.DetPanel;
import kvv.gwtutils.client.form.EditablePanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public abstract class ExprButton extends Composite {
	private final HorizontalPanel panel = new HorizontalPanel();
	private final VerticalPanel vp = new VerticalPanel();
	private final VerticalPanel details = new VerticalPanel();
	private final CheckBox cb = new CheckBox();
	private final HorizontalPanel savePanel = new HorizontalPanel();
	private final Button save = new Button("Сохранить");

	private final DetPanel<Label, VerticalPanel> detailsPanel;

	private final EditablePanel<ExprContol> expressions = new EditablePanel<ExprContol>(
			!ModePage.controlMode, "Добавить выражение") {
		@Override
		protected ExprContol createItem() {
			return new ExprContol("", null);
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

	public ExprButton() {

		cb.setValue(false);
		// cb.setEnabled(false);
		cb.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (registerSchedule == null)
					return;

				if (!ModePage.check()) {
					cb.setValue(!cb.getValue());
					return;
				}

				registerSchedule.state = cb.getValue() ? State.EXPRESSION
						: State.MANUAL;
				save(registerSchedule);
			}
		});

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
				save(registerSchedule);
			}
		});

		panel.add(cb);

		details.add(expressions);
		if (ModePage.controlMode) {
			savePanel.add(save);
			details.add(savePanel);
		}

		detailsPanel = new DetPanel<Label, VerticalPanel>("Формула", details);

		vp.add(detailsPanel);

		panel.add(vp);

		initWidget(panel);
	}

	public void updateUI(RegisterSchedule registerSchedule) {
		this.registerSchedule = registerSchedule;
		cb.setValue(registerSchedule.state == State.EXPRESSION);

		boolean err = false;

		expressions.clear();
		for (Expr e : registerSchedule.expressions) {
			expressions.add(new ExprContol(e.expr, e.errMsg));
			err |= (e.errMsg != null);
		}

		detailsPanel.label.getElement().getStyle()
				.setColor(err ? "red" : "black");

	}

	public abstract void save(RegisterSchedule registerSchedule);
}
