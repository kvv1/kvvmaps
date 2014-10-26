package kvv.controllers.client.control.simple;

import kvv.controllers.client.ScheduleService;
import kvv.controllers.client.ScheduleServiceAsync;
import kvv.controllers.client.control.form.DetailsPanel;
import kvv.controllers.client.page.ModePage;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.RegisterSchedule.Expr;
import kvv.controllers.shared.RegisterSchedule.State;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public abstract class ExprButton extends Composite {
	private final HorizontalPanel panel = new HorizontalPanel();
	private final VerticalPanel vp = new VerticalPanel();
	private final VerticalPanel details = new VerticalPanel();
	private final CheckBox cb = new CheckBox();
	private final Button save = new Button("Set");
	private final Button add = new Button("Add");

	private final DetailsPanel detailsPanel;

	private final VerticalPanel expressions = new VerticalPanel();

	private RegisterSchedule registerSchedule;

	private final ScheduleServiceAsync scheduleService = GWT
			.create(ScheduleService.class);

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
				for (int i = 0; i < expressions.getWidgetCount(); i++) {
					ExprContol exprContol = (ExprContol) expressions
							.getWidget(i);
					if (!exprContol.getText().trim().isEmpty())
						registerSchedule.expressions.add(new Expr(exprContol
								.getText()));
				}
				save(registerSchedule);
			}
		});

		add.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				expressions.add(new ExprContol("", null));
			}
		});

		panel.add(cb);

		details.add(expressions);
		if (ModePage.controlMode) {
			HorizontalPanel hp = new HorizontalPanel();
			hp.add(save);
			hp.add(add);
			details.add(hp);
		}

		detailsPanel = new DetailsPanel("Формула", details);

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

		detailsPanel.b.getElement().getStyle().setColor(err ? "red" : "black");

	}

	public abstract void save(RegisterSchedule registerSchedule);
}
