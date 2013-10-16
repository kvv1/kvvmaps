package kvv.controllers.client.page;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.page.source.ExtendedTextArea;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public abstract class TextWithSaveButton extends Composite {
	private VerticalPanel vertPanel = new VerticalPanel();
	private TextArea text = new ExtendedTextArea() {
		@Override
		protected void onChanged() {
			dirty = true;
			save.setEnabled(dirty);
		}
	};
	private boolean dirty;
	private Button save = new Button("Сохранить");

	protected abstract void save(String text, AsyncCallback<Void> callback);

	public TextWithSaveButton(String caption, String width, String height) {
		text.setSize(width, height);

		if (caption != null)
			vertPanel.add(new Label(caption));

		vertPanel.add(text);
		vertPanel.add(save);

		dirty = false;
		save.setEnabled(dirty);

		save.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				save(text.getText(), new CallbackAdapter<Void>() {
					@Override
					public void onSuccess(Void result) {
						dirty = false;
						save.setEnabled(dirty);
					}
				});
			}
		});

		initWidget(vertPanel);
	}

	// setS

	public void setText(String result) {
		text.setText(result);
		dirty = false;
		save.setEnabled(dirty);
	}
}
