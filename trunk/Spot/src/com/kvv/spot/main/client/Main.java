package com.kvv.spot.main.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.kvv.spot.main.shared.SpotRemoveMethod;

public class Main implements EntryPoint {
	RootPanel rootPanel;

	private final SpotServiceAsync spotService = GWT.create(SpotService.class);

	public void onModuleLoad() {
		rootPanel = RootPanel.get();

		rootPanel.add(new HTML("<a href='spot/protected/index.jsp'>again</a>"));
		
		final ButtonPanel buttonPanel = new ButtonPanel();

		System.out
				.println("GWT.getModuleBaseURL() = " + GWT.getModuleBaseURL());

		UploadForm uploadForm = new UploadForm(GWT.getModuleBaseURL()
				+ "uploadImage");
		final ImageEditor ed = new ImageEditor();

		uploadForm.addSubmitCompleteHandler(new SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				buttonPanel.reset();
				ed.cancel();
				ed.load(GWT.getModuleBaseURL() + "uploadImage");
			}
		});

		VerticalPanel vertPanel = new VerticalPanel();
		vertPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		vertPanel.setSpacing(10);
		// horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		vertPanel.add(uploadForm);
		vertPanel.add(buttonPanel);

		
		PushButton downloadButton = new PushButton("Download result",
				new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						Window.open(GWT.getModuleBaseURL() + "uploadImage", "",
								"");
					}
				});

		vertPanel.add(downloadButton);

		// ScrollPanel scrollPanel = new ScrollPanel(ed);
		// scrollPanel.setSize("1024px", "700px");

		HorizontalPanel horPanel = new HorizontalPanel();
		horPanel.add(vertPanel);
		horPanel.add(ed);

		rootPanel.add(horPanel);

		buttonPanel.removeSpotButton
				.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						if (event.getValue())
							ed.tryRemoveSpot(SpotRemoveMethod.VH);
						else
							ed.cancel();
					}
				});

		buttonPanel.removeSpotVButton
				.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						if (event.getValue())
							ed.tryRemoveSpot(SpotRemoveMethod.V);
						else
							ed.cancel();
					}
				});

		buttonPanel.removeSpotHButton
				.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						if (event.getValue())
							ed.tryRemoveSpot(SpotRemoveMethod.H);
						else
							ed.cancel();
					}
				});

		buttonPanel.okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ed.confirm();
				buttonPanel.reset();
			}
		});
	}

}
// ImageElement imgMap = Document.get().createImageElement();
// imgMap.setSrc(src);
// ImageElement.as(image.getElement()