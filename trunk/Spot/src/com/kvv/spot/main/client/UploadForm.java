package com.kvv.spot.main.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class UploadForm extends FormPanel {
	public UploadForm(String uploadURL) {
		setAction(uploadURL);
		setEncoding(FormPanel.ENCODING_MULTIPART);
		setMethod(FormPanel.METHOD_POST);

		VerticalPanel panel = new VerticalPanel();
		setWidget(panel);

		FileUpload upload = new FileUpload();
		upload.setStyleName("");
		upload.setName("image");
		panel.add(upload);

		Button button = new Button("Upload image", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				submit();
			}
		});
		button.setStyleName("");
		panel.add(button);
	}
}
