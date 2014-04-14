package kvv.controllers.client.control.vm;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.control.ChildComposite;
import kvv.controllers.client.page.ModePage;
import kvv.controllers.register.AllRegs;
import kvv.controllers.register.ControllerDef;
import kvv.controllers.register.Operation;
import kvv.controllers.register.Rule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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

	private static String[] ops = { "==", "!=", "<", ">", "<=", ">=" };

	private int addr;
	private VerticalPanel panel = new VerticalPanel();

	class RuleRow extends HorizontalPanel {
		CheckBox enabled = new CheckBox();
		TextBox srcReg = new TextBox();
		ListBox operation = new ListBox();
		TextBox srcVal = new TextBox();
		TextBox dstReg = new TextBox();
		TextBox dstVal = new TextBox();
		{
			srcReg.setWidth("40px");
			srcVal.setWidth("40px");
			dstReg.setWidth("40px");
			dstVal.setWidth("40px");
			add(enabled);
			add(srcReg);
			for (int i = 0; i < Operation.values().length; i++)
				operation.addItem(ops[i]);
			add(operation);
			add(srcVal);
			add(new Label("->"));
			add(dstReg);
			add(new Label("="));
			add(dstVal);
		}
	}

	public RuleControl(final int addr, ControllerDef def) {
		super(addr);

		this.addr = addr;

		panel.setBorderWidth(2);

		for (int i = 0; i < 8; i++) {
			panel.add(new RuleRow());
		}

		Button upl = new Button("Upload");
		upl.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!ModePage.check())
					return;
				// controllersService.setRule(addr, file,
				// new CallbackAdapter<String>() {
				// @Override
				// public void onSuccess(String result) {
				// if (result != null) {
				// Window.alert(result);
				// return;
				// }
				// refresh();
				// }
				// });
			}
		});

		panel.add(upl);
		initWidget(panel);
	}

	@Override
	public void refresh(AllRegs result) {
		controllersService.getRules(addr, new CallbackAdapter<Rule[]>() {

			// @Override
			// public void onFailure(Throwable caught) {
			// // TODO Auto-generated method stub
			//
			// }

			@Override
			public void onSuccess(Rule[] result) {
				for (int i = 0; i < result.length; i++) {
					Rule rule = result[i];
					RuleRow rr = (RuleRow) panel.getWidget(i);
					rr.enabled.setValue(rule.en);
					rr.srcReg.setText(rule.srcReg + "");
					rr.operation.setSelectedIndex(rule.op.ordinal());
					rr.srcVal.setText(rule.srcVal + "");
					rr.dstReg.setText(rule.dstReg + "");
					rr.dstVal.setText(rule.dstVal + "");
				}

				// TODO Auto-generated method stub

			}
		});
	}
}
