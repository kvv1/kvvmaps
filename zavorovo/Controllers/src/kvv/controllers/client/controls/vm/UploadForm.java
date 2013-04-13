package kvv.controllers.client.controls.vm;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class UploadForm extends FormPanel {
	public UploadForm(String uploadURL) {
		setAction(uploadURL);
		setEncoding(FormPanel.ENCODING_MULTIPART);
		setMethod(FormPanel.METHOD_POST);

		HorizontalPanel panel = new HorizontalPanel();
		setWidget(panel);

		FileUpload upload = new FileUpload();
		upload.setStyleName("");
		upload.setName("image");
		panel.add(upload);
		
		upload.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				// TODO Auto-generated method stub
				
			}
		});

		Button button = new Button("Upload", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				submit();
			}
		});
		button.setStyleName("");
		panel.add(button);
	}
}
