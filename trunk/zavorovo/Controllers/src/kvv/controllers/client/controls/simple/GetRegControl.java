package kvv.controllers.client.controls.simple;

import java.util.Map;

import kvv.controllers.client.ControllersServiceAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class GetRegControl extends Composite {

	private final HorizontalPanel panel = new HorizontalPanel();

	private final int addr;
	private final Label label;
	private final ControllersServiceAsync controllersService;
	private final Label edit;
	private final int reg;
	private final boolean div10;

	public GetRegControl(final int addr, final int reg, final boolean div10,
			String text, final ControllersServiceAsync controllersService) {
		this.addr = addr;
		this.label = new Label(text);
		this.edit = new Label();
		this.reg = reg;
		this.div10 = div10;
		this.controllersService = controllersService;

		edit.setWidth("40px");
		// edit.setReadOnly(true);
		edit.setText("???");

		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		// panel.setSpacing(10);
		// panel.setBorderWidth(1);

		panel.add(label);
		panel.add(edit);

		initWidget(panel);
	}

	
	public void refresh(Map<Integer, Integer> regs) {
		edit.setText("???");
		
		if (regs == null)
			return;
		
		Integer _val = regs.get(reg);
		
		if (_val == null)
			return;

		if (div10)
			edit.setText(Float.toString((float) _val / 10));
		else
			edit.setText(Integer.toString(_val));
	}

	
	
	public void refresh() {
		edit.setText("???");
		controllersService.getReg(addr, reg, new AsyncCallback<Integer>() {
			@Override
			public void onSuccess(Integer result) {
				if (div10)
					edit.setText(Float.toString((float) result / 10));
				else
					edit.setText(Integer.toString(result));
			}

			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}

}
