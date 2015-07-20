package kvv.controllers.client.control.vm;

import java.util.ArrayList;
import java.util.List;

import kvv.controller.register.AllRegs;
import kvv.controller.register.ControllerDef;
import kvv.controller.register.Operation;
import kvv.controller.register.Rule;
import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.control.ChildComposite;
import kvv.controllers.client.page.ModePage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class RuleControl extends ChildComposite {

	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	private static String[] ops = { "", "==", "!=", "<", ">", "<=", ">=" };

	private int addr;
	private VerticalPanel panel = new VerticalPanel();
	private VerticalPanel rulesPanel = new VerticalPanel();

	class RuleRow extends HorizontalPanel {
		CheckBox enabled = new CheckBox();
		TextBox srcReg = new TextBox();
		ListBox operation = new ListBox();
		TextBox srcVal = new TextBox();
		TextBox dstReg = new TextBox();
		TextBox dstVal = new TextBox();
		{
			setVerticalAlignment(ALIGN_MIDDLE);
			srcReg.setWidth("40px");
			srcVal.setWidth("40px");
			dstReg.setWidth("40px");
			dstVal.setWidth("40px");
			add(enabled);
			add(new Label("R"));
			add(srcReg);
			for (int i = 0; i < Operation.values().length; i++)
				operation.addItem(ops[i]);
			add(operation);
			add(srcVal);
			add(new Label("->"));
			add(new Label("R"));
			add(dstReg);
			add(new Label("="));
			add(dstVal);
		}
	}

	public RuleControl(final int addr, ControllerDef def) {
		super(addr);

		this.addr = addr;

		panel.setBorderWidth(2);
		panel.add(rulesPanel);

		Button set = new Button("Set");
		set.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!ModePage.check())
					return;

				List<Rule> rules = new ArrayList<Rule>();
				try {
					for (int i = 0; i < rulesPanel.getWidgetCount(); i++) {
						RuleRow rr = (RuleRow) rulesPanel.getWidget(i);
						Rule rule = new Rule();

						rule.en = rr.enabled.getValue();
						rule.srcReg = Integer.parseInt(rr.srcReg.getText());
						rule.op = Operation.values()[rr.operation
								.getSelectedIndex()];
						rule.srcVal = Integer.parseInt(rr.srcVal.getText());
						rule.dstReg = Integer.parseInt(rr.dstReg.getText());
						rule.dstVal = Integer.parseInt(rr.dstVal.getText());

						rules.add(rule);
					}

					rulesPanel.clear();

					controllersService.setRules(addr,
							rules.toArray(new Rule[0]),
							new CallbackAdapter<Void>() {
								@Override
								public void onSuccess(Void result) {
									refresh();
								}
							});
				} catch (Exception e) {
					Window.alert(e.getMessage());
				}
			}
		});

		Button update = new Button("Обновить");
		update.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refresh();
			}
		});

		panel.add(set);
		panel.add(update);
		initWidget(panel);
	}

	@Override
	public void refresh(AllRegs result) {
		rulesPanel.clear();
		if (result != null) {
			controllersService.getRules(addr, new CallbackAdapter<Rule[]>() {

				@Override
				public void onSuccess(Rule[] result) {
					for (int i = 0; i < result.length; i++) {
						Rule rule = result[i];
						RuleRow rr = new RuleRow();
						rr.enabled.setValue(rule.en);
						rr.srcReg.setText(rule.srcReg + "");
						rr.operation.setSelectedIndex(rule.op.ordinal());
						rr.srcVal.setText(rule.srcVal + "");
						rr.dstReg.setText(rule.dstReg + "");
						rr.dstVal.setText(rule.dstVal + "");
						rulesPanel.add(rr);
					}
				}
			});
		}
	}
}
