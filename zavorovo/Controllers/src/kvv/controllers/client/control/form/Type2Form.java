package kvv.controllers.client.control.form;

import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.control.ControlComposite;
import kvv.controllers.client.control.simple.GetRegControl;
import kvv.controllers.client.control.simple.RelayCheckBoxes;
import kvv.controllers.client.control.vm.VMControl;
import kvv.controllers.register.Register;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class Type2Form extends ControlComposite {

	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	public Type2Form(final int addr, String name) {
		HorizontalPanel panel = new HorizontalPanel();
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setSpacing(10);

		Label nameLabel = new Label(name);
		nameLabel.setWidth("100px");

		panel.add(nameLabel);

		Label addrLabel = new Label("addr=" + addr);
		addrLabel.setWidth("70px");

		panel.add(addrLabel);

		RelayCheckBoxes relays = new RelayCheckBoxes(addr, Register.REG_RELAY0,
				(int) Register.REG_PWM0, Register.REG_RELAY_CNT);
		add(relays);
		panel.add(relays);

		VerticalPanel tempPanel = new VerticalPanel();

		GetRegControl tempVal = new GetRegControl(addr, Register.REG_TEMP, 1,
				"T1=");
		add(tempVal);
		tempPanel.add(tempVal);

		GetRegControl tempVal2 = new GetRegControl(addr, Register.REG_TEMP2, 1,
				"T2=");
		add(tempVal2);
		tempPanel.add(tempVal2);

		panel.add(tempPanel);

		// GetRegControl vVal = new GetRegControl(addr, Register.REG_ADC3,
		// 0.0202f, "V=");
		// add(vVal);
		// panel.add(vVal);

		VMControl vmControl = new VMControl(addr, name);
		add(vmControl);
		panel.add(vmControl);

		Button helloButton = new Button("Hello");
		panel.add(helloButton);

		helloButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				controllersService.hello(addr, new AsyncCallback<Integer>() {

					@Override
					public void onSuccess(Integer result) {
						Window.alert("OK " + result);
					}

					@Override
					public void onFailure(Throwable caught) {
						Window.alert("FAILED " + caught.getMessage());
					}
				});

				// refresh();
			}
		});

		panel.add(new UploadForm("Загрузить", GWT.getModuleBaseURL()
				+ "upload?addr=" + addr));

		final Timer refreshTimer = new Timer() {
			public void run() {
				refresh();
				schedule(5000);
			}
		};

		Button refreshButton = new Button("Обновить");
		panel.add(refreshButton);

		refreshButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refresh();
			}
		});

		final CheckBox autorefresh = new CheckBox("Автообновление");
		autorefresh.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (autorefresh.getValue())
					refreshTimer.schedule(100);
				else
					refreshTimer.cancel();
			}
		});

		panel.add(autorefresh);

		initWidget(panel);
		refresh();
	}

	FormPanel addUpload() {
		VerticalPanel panel = new VerticalPanel();
		panel.setBorderWidth(1);
		// create a FormPanel
		final FormPanel form = new FormPanel();
		// create a file upload widget
		final FileUpload fileUpload = new FileUpload();
		// create labels
		// Label selectLabel = new Label("Select a file:");
		// create upload button
		Button uploadButton = new Button("Загрузить программу");
		// pass action to the form to point to service handling file
		// receiving operation.
		form.setAction(GWT.getModuleBaseURL() + "upload");
		// set form to use the POST method, and multipart MIME encoding.
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setMethod(FormPanel.METHOD_POST);

		// add a label
		// panel.add(selectLabel);
		// add fileUpload widget
		panel.add(fileUpload);
		// add a button to upload the file
		panel.add(uploadButton);
		uploadButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// get the filename to be uploaded
				String filename = fileUpload.getFilename();
				if (filename.length() == 0) {
					Window.alert("No File Specified!");
				} else {
					// submit the form
					form.submit();
				}
			}
		});

		form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				// When the form submission is successfully completed, this
				// event is fired. Assuming the service returned a response
				// of type text/html, we can get the result text here
				Window.alert(event.getResults());
			}
		});
		panel.setSpacing(10);

		// Add form to the root panel.
		form.add(panel);
		return form;
	}

}
