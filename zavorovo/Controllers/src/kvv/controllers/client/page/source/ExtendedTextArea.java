package kvv.controllers.client.page.source;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.TextArea;

public abstract class ExtendedTextArea extends TextArea {

	protected abstract void onChanged();

	public ExtendedTextArea() {
		super();
		sinkEvents(Event.ONPASTE);
		getElement().setAttribute("spellCheck", "false");
		addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				onChanged();
			}
		});
		addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				onChanged();
			}
		});

		addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				onChanged();
			}
		});

		KeyDownHandler DEFAULT_TEXTAREA_TAB_HANDLER = new KeyDownHandler() {
			@Override
			public final void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == 9) {
					event.preventDefault();
					event.stopPropagation();
					final TextArea ta = (TextArea) event.getSource();
					final int index = ta.getCursorPos();
					final String text = ta.getText();
					ta.setText(text.substring(0, index) + "\t"
							+ text.substring(index));
					ta.setCursorPos(index + 1);
				}
			}
		};

		addKeyDownHandler(DEFAULT_TEXTAREA_TAB_HANDLER);

	}

	@Override
	public void onBrowserEvent(Event event) {
		super.onBrowserEvent(event);
		switch (DOM.eventGetType(event)) {
		case Event.ONPASTE:
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {

				@Override
				public void execute() {
					ValueChangeEvent.fire(ExtendedTextArea.this, getText());
				}

			});
			break;
		}
	}
}