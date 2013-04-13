package kvv.controllers.client.controls.vm;

import java.util.Map;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.SourcesService;
import kvv.controllers.client.SourcesServiceAsync;
import kvv.controllers.client.controls.ControlComposite;
import kvv.controllers.client.controls.simple.GetRegControl;
import kvv.controllers.client.controls.simple.GetSetRegControl;
import kvv.controllers.client.controls.simple.SimpleRelayControl;
import kvv.controllers.register.Register;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;

public class VMControl extends ControlComposite {

	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	private final SourcesServiceAsync sourcesService = GWT
			.create(SourcesService.class);

	private final SimpleRelayControl vmCheckBox;

	private final GetRegControl vmState;

	private final GetSetRegControl eeprom0;
	private final GetSetRegControl eeprom1;
	private final GetSetRegControl eeprom2;
	private final GetSetRegControl eeprom3;

	private final GetRegControl ram0;
	private final GetRegControl ram1;
	private final GetRegControl ram2;
	private final GetRegControl ram3;

	private final ListBox files;

	private final Grid panel = new Grid(2, 6);

	private final String name;

	public VMControl(final int addr, final String name) {
		super(addr);

		this.name = name;

		vmCheckBox = new SimpleRelayControl(addr, Register.REG_VM);
		panel.setWidget(0, 0, vmCheckBox);

		vmState = new GetRegControl(addr, Register.REG_VM, false, "VM=");
		panel.setWidget(1, 0, vmState);

		eeprom0 = new GetSetRegControl(addr, Register.REG_EEPROM0, false, "");
		panel.setWidget(0, 1, eeprom0);
		eeprom1 = new GetSetRegControl(addr, Register.REG_EEPROM1, false, "");
		panel.setWidget(0, 2, eeprom1);
		eeprom2 = new GetSetRegControl(addr, Register.REG_EEPROM2, false, "");
		panel.setWidget(0, 3, eeprom2);
		eeprom3 = new GetSetRegControl(addr, Register.REG_EEPROM3, false, "");
		panel.setWidget(0, 4, eeprom3);

		ram0 = new GetRegControl(addr, Register.REG_RAM0, false, "");
		panel.setWidget(1, 1, ram0);
		ram1 = new GetRegControl(addr, Register.REG_RAM1, false, "");
		panel.setWidget(1, 2, ram1);
		ram2 = new GetRegControl(addr, Register.REG_RAM2, false, "");
		panel.setWidget(1, 3, ram2);
		ram3 = new GetRegControl(addr, Register.REG_RAM3, false, "");
		panel.setWidget(1, 4, ram3);

		files = new ListBox();

		Button upl = new Button("Upload");
		upl.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final String file;
				if (files.getSelectedIndex() <= 0)
					file = null;
				else
					file = files.getItemText(files.getSelectedIndex());
				refresh(null);
				controllersService.upload(addr, file,
						new CallbackAdapter<String>() {
							@Override
							public void onSuccess(String result) {
								if (result != null) {
									Window.alert(result);
									return;
								}

								sourcesService.setSourceFile(name, file,
										new CallbackAdapter<Void>() {
											@Override
											public void onSuccess(Void result) {
												refresh();
											}
										});
							}
						});

			}
		});

		panel.setWidget(0, 5, files);
		panel.setWidget(1, 5, upl);

		HorizontalPanel p = new HorizontalPanel();
		p.setBorderWidth(1);
		p.add(panel);
		initWidget(p);
	}

	public void refresh(Map<Integer, Integer> result) {
		vmCheckBox.refresh(result);
		vmState.refresh(result);
		ram0.refresh(result);
		ram1.refresh(result);
		ram2.refresh(result);
		ram3.refresh(result);
		eeprom0.refresh(result);
		eeprom1.refresh(result);
		eeprom2.refresh(result);
		eeprom3.refresh(result);

		files.setEnabled(false);
		if (result != null)
			sourcesService.getSourceFiles(new CallbackAdapter<String[]>() {
				@Override
				public void onSuccess(String[] result) {
					files.setEnabled(true);
					files.clear();
					files.addItem("<no source>");
					for (String name : result)
						files.addItem(name);
					sourcesService.getSourceFile(name,
							new CallbackAdapter<String>() {
								@Override
								public void onSuccess(String result) {
									for (int i = 0; i < files.getItemCount(); i++)
										if (files.getItemText(i).equals(result)) {
											files.setSelectedIndex(i);
											break;
										}
								}
							});
				}
			});
	}
}
