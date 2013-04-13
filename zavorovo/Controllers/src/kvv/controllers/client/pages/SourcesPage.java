package kvv.controllers.client.pages;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.SourcesService;
import kvv.controllers.client.SourcesServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SourcesPage extends Composite {
	private final SourcesServiceAsync sourcesService = GWT
			.create(SourcesService.class);

	public SourcesPage() {
		HorizontalPanel panel = new HorizontalPanel();

		Button buttonNew = new Button("New");
		buttonNew.setWidth("100%");
		Button buttonOpen = new Button("Open");
		buttonOpen.setWidth("100%");
		Button buttonDel = new Button("Delete");
		buttonDel.setWidth("100%");

		final ListBox files = new ListBox();
		files.setWidth("100%");
		files.setVisibleItemCount(20);

		sourcesService.getSourceFiles(new CallbackAdapter<String[]>() {
			@Override
			public void onSuccess(String[] result) {
				for (String name : result)
					files.addItem(name);
			}
		});

		VerticalPanel buttons = new VerticalPanel();
		buttons.setWidth("100px");

		buttons.add(files);
		buttons.add(buttonOpen);
		buttons.add(buttonNew);
		buttons.add(buttonDel);

		final TabPanel tabs = new TabPanel();
		tabs.setHeight("200px");
		tabs.setWidth("800px");

		panel.add(buttons);
		panel.add(tabs);

		buttonOpen.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				int idx = files.getSelectedIndex();
				if (idx < 0)
					return;
				String name = files.getItemText(idx);

				for (int i = 0; i < tabs.getWidgetCount(); i++)
					if (((SourcePage) tabs.getWidget(i)).getName().equals(name)) {
						tabs.selectTab(i);
						return;
					}

				final SourcePage page = new SourcePage(name) {
					@Override
					protected void onClose() {
						for (int i = 0; i < tabs.getWidgetCount(); i++)
							if (((SourcePage) tabs.getWidget(i)).getName()
									.equals(getName())) {
								tabs.remove(i);
								return;
							}
					}
				};

				tabs.add(page, name);
				tabs.selectTab(tabs.getWidgetCount() - 1);

			}
		});

		initWidget(panel);
	}
}
