package kvv.controllers.client.pages.source;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.SourcesService;
import kvv.controllers.client.SourcesServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public abstract class SourcePage extends Composite {
	private VerticalPanel vertPanel = new VerticalPanel();
	private ExtendedTextArea text = new ExtendedTextArea();
	private Button save = new Button("Сохранить");
	private Button close = new Button("Закрыть");

	private boolean dirty;

	private final SourcesServiceAsync sourcesService = GWT
			.create(SourcesService.class);

	private final String name;

	protected abstract void onClose();

	public String getName() {
		return name;
	}

	void updateUI() {
		save.setEnabled(dirty);
	}

	public SourcePage(final String name) {
		this.name = name;
		text.setSize("800px", "600px");
		text.getElement().setAttribute("spellCheck", "false");

		FlowPanel buttons = new FlowPanel();
		buttons.add(save);
		buttons.add(close);

		vertPanel.add(text);
		vertPanel.add(buttons);

		initWidget(vertPanel);

		text.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				dirty = true;
				updateUI();
			}
		});
		text.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				dirty = true;
				updateUI();
			}
		});

		text.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				dirty = true;
				updateUI();
			}
		});

		sourcesService.getSource(name, new CallbackAdapter<String>() {
			@Override
			public void onSuccess(String result) {
				text.setValue(result, false);
				updateUI();
			}
		});

		save.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sourcesService.setSource(name, text.getText(),
						new CallbackAdapter<Void>() {
							@Override
							public void onSuccess(Void result) {
								dirty = false;
								updateUI();
							}
						});
			}
		});

		close.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onClose();
			}

		});
	}
}

class ExtendedTextArea extends TextArea {

	public ExtendedTextArea() {
		super();
		sinkEvents(Event.ONPASTE);
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