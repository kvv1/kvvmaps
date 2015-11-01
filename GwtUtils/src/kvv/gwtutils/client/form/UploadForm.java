package kvv.gwtutils.client.form;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public abstract class UploadForm extends FormPanel {
	public UploadForm(String buttonText) {
		setEncoding(FormPanel.ENCODING_MULTIPART);
		setMethod(FormPanel.METHOD_POST);

		VerticalPanel panel = new VerticalPanel();
		setWidget(panel);

		FileUpload upload = new FileUpload();
		upload.setStyleName("");
		upload.setName("image");
		panel.add(upload);

		Button button = new Button(buttonText, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setAction(getUrl());
				submit();
			}
		});

		addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				// When the form submission is successfully completed, this
				// event is fired. Assuming the service returned a response
				// of type text/html, we can get the result text here
				Window.alert(event.getResults());
			}
		});

		// upload.setStyleName(button.getStyleName());

		button.setStyleName("");
		panel.add(button);
	}
	
	public abstract String getUrl();
}
