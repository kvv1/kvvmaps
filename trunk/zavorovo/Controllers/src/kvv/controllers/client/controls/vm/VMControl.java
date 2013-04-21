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
import kvv.controllers.register.SourceDescr;

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

	private final ListBox files;

	private final Grid panel = new Grid(2, 6);

	private final String name;

	public VMControl(final int addr, final String name) {
		super(addr);

		this.name = name;

		SimpleRelayControl vmCheckBox = new SimpleRelayControl(addr,
				Register.REG_VMONOFF, null);
		add(vmCheckBox);
		panel.setWidget(0, 0, vmCheckBox);

		GetRegControl vmState = new GetRegControl(addr, Register.REG_VMSTATE,
				false, "VM=");
		add(vmState);
		panel.setWidget(1, 0, vmState);

		GetSetRegControl eeprom0 = new GetSetRegControl(addr,
				Register.REG_EEPROM0, false, "");
		add(eeprom0);
		panel.setWidget(0, 1, eeprom0);
		GetSetRegControl eeprom1 = new GetSetRegControl(addr,
				Register.REG_EEPROM1, false, "");
		add(eeprom1);
		panel.setWidget(0, 2, eeprom1);
		GetSetRegControl eeprom2 = new GetSetRegControl(addr,
				Register.REG_EEPROM2, false, "");
		add(eeprom2);
		panel.setWidget(0, 3, eeprom2);
		GetSetRegControl eeprom3 = new GetSetRegControl(addr,
				Register.REG_EEPROM3, false, "");
		add(eeprom3);
		panel.setWidget(0, 4, eeprom3);

		GetRegControl ram0 = new GetRegControl(addr, Register.REG_RAM0, false,
				"");
		add(ram0);
		panel.setWidget(1, 1, ram0);
		GetRegControl ram1 = new GetRegControl(addr, Register.REG_RAM1, false,
				"");
		add(ram1);
		panel.setWidget(1, 2, ram1);
		GetRegControl ram2 = new GetRegControl(addr, Register.REG_RAM2, false,
				"");
		add(ram2);
		panel.setWidget(1, 3, ram2);
		GetRegControl ram3 = new GetRegControl(addr, Register.REG_RAM3, false,
				"");
		add(ram3);
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
								refresh();
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
		super.refresh(result);

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
					sourcesService.getSourceDescr(name,
							new CallbackAdapter<SourceDescr>() {
								@Override
								public void onSuccess(SourceDescr result) {
									for (int i = 0; i < files.getItemCount(); i++)
										if (result != null
												&& files.getItemText(i).equals(
														result.filename)) {
											files.setSelectedIndex(i);
											break;
										}
								}
							});
				}
			});
	}
}
