package kvv.controllers.client.page.source;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.SourcesService;
import kvv.controllers.client.SourcesServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public abstract class SourcePage extends Composite {
	private VerticalPanel vertPanel = new VerticalPanel();
	private ExtendedTextArea text = new ExtendedTextArea() {
		@Override
		protected void onChanged() {
			dirty = true;
			updateUI();
		}
	};
	private boolean dirty;

	private Button save = new Button("Сохранить");
	private Button close = new Button("Закрыть");

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

		FlowPanel buttons = new FlowPanel();
		buttons.add(save);
		buttons.add(close);

		vertPanel.add(text);
		vertPanel.add(buttons);

		initWidget(vertPanel);

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