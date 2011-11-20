package com.kvv.spot.main.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ButtonPanel extends Composite {
	public final ToggleButton removeSpotButton = new ToggleButton("Remove spot");
	public final ToggleButton removeSpotHButton = new ToggleButton(
			"Remove spot H");
	public final ToggleButton removeSpotVButton = new ToggleButton(
			"Remove spot V");
	public final PushButton okButton = new PushButton("OK");

	private VerticalPanel buttonPanel = new VerticalPanel();

	public ButtonPanel() {
		buttonPanel.setSpacing(10);
		buttonPanel.add(removeSpotButton);
		buttonPanel.add(removeSpotHButton);
		buttonPanel.add(removeSpotVButton);
		buttonPanel.add(okButton);

		removeSpotButton
				.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						removeSpotHButton.setValue(false);
						removeSpotVButton.setValue(false);
					}
				});

		removeSpotVButton
				.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						removeSpotHButton.setValue(false);
						removeSpotButton.setValue(false);
					}
				});

		removeSpotHButton
				.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						removeSpotVButton.setValue(false);
						removeSpotButton.setValue(false);
					}
				});

		initWidget(buttonPanel);
	}

	public void reset() {
		removeSpotButton.setDown(false);
		removeSpotHButton.setDown(false);
		removeSpotVButton.setDown(false);
	}
}
