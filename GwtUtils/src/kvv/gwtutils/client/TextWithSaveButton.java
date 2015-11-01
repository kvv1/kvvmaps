package kvv.gwtutils.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

@SuppressWarnings("deprecation")
public abstract class TextWithSaveButton extends Composite {
	private VerticalPanel vertPanel = new VerticalPanel();
	private TextArea text = new ExtendedTextArea() {
		{
			addFocusListener(new FocusListener() {
				@Override
				public void onFocus(Widget sender) {
					focused = true;
				}

				public void onLostFocus(Widget sender) {
					new Timer() {
						@Override
						public void run() {
							focused = false;
						}
					}.schedule(500);
				}
			});
		}
		
		@Override
		protected void onChanged() {
			dirty = true;
			save.setEnabled(dirty);
		}
	};

	public boolean focused;

	private boolean dirty;
	private Button save = new Button("Сохранить");

	protected abstract void save(String text, AsyncCallback<Void> callback);

	public TextWithSaveButton(String caption, String width, String height) {
		text.setSize("100%", height);

		if (caption != null)
			vertPanel.add(new Label(caption));

		vertPanel.add(text);
		vertPanel.add(save);

		dirty = false;
		save.setEnabled(dirty);

		save.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				save(text.getText(), new AsyncCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						dirty = false;
						save.setEnabled(dirty);
					}

					@Override
					public void onFailure(Throwable caught) {
					}
				});
			}
		});

		vertPanel.setWidth(width);
		
		initWidget(vertPanel);
	}

	// setS

	public void setText(String result) {
		text.setText(result);
		dirty = false;
		save.setEnabled(dirty);
	}
}
