package kvv.controllers.client.page;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.SourcesService;
import kvv.controllers.client.SourcesServiceAsync;
import kvv.controllers.client.page.source.SourcePage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SourcesPage extends Composite {
	private final SourcesServiceAsync sourcesService = GWT
			.create(SourcesService.class);

	private ListBox files = new ListBox();
	private TabPanel tabs = new TabPanel();

	public SourcesPage() {
		HorizontalPanel panel = new HorizontalPanel();

		Button buttonNew = new Button("New");
		buttonNew.setWidth("100%");
		Button buttonOpen = new Button("Open");
		buttonOpen.setWidth("100%");
		Button buttonDel = new Button("Delete");
		buttonDel.setWidth("100%");

		files.setWidth("100%");
		files.setVisibleItemCount(20);

		refreshFiles();

		VerticalPanel buttons = new VerticalPanel();
		buttons.setWidth("100px");

		final TextBox newFileName = new TextBox();
		buttons.add(newFileName);
		buttons.add(buttonNew);

		buttons.add(files);
		buttons.add(buttonOpen);
		buttons.add(buttonDel);

		tabs.setHeight("200px");
		tabs.setWidth("800px");

		panel.add(buttons);
		panel.add(tabs);

		buttonOpen.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String name = getSelectedFile();
				openFile(name);
			}
		});

		buttonDel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String name = getSelectedFile();
				if (!Window.confirm("Удалить " + name + "?"))
					return;
				sourcesService.delSourceFile(name, new CallbackAdapter<Void>() {
					@Override
					public void onSuccess(Void result) {
						refreshFiles();
					}
				});
			}
		});

		buttonNew.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				final String name = newFileName.getText();
				sourcesService.createSource(name,
						new CallbackAdapter<String>() {
							@Override
							public void onSuccess(String result) {
								refreshFiles();
								openFile(name);
							}
						});
			}
		});

		initWidget(panel);
	}

	private String getSelectedFile() {
		int idx = files.getSelectedIndex();
		if (idx < 0)
			return null;
		return files.getItemText(idx);
	}

	private void openFile(String name) {
		for (int i = 0; i < tabs.getWidgetCount(); i++)
			if (((SourcePage) tabs.getWidget(i)).getName().equals(name)) {
				tabs.selectTab(i);
				return;
			}

		final SourcePage page = new SourcePage(name) {
			@Override
			protected void onClose() {
				for (int i = 0; i < tabs.getWidgetCount(); i++)
					if (((SourcePage) tabs.getWidget(i)).getName().equals(
							getName())) {
						tabs.remove(i);
						return;
					}
			}
		};

		tabs.add(page, name);
		tabs.selectTab(tabs.getWidgetCount() - 1);

	}

	private void refreshFiles() {
		files.clear();
		sourcesService.getSourceFiles(new CallbackAdapter<String[]>() {
			@Override
			public void onSuccess(String[] result) {
				for (String name : result)
					files.addItem(name);
			}
		});

	}
}
