package kvv.controllers.client;

import kvv.controllers.client.page.ConfigurationPage;
import kvv.controllers.client.page.ControllersPage;
import kvv.controllers.client.page.RegistersPage;
import kvv.controllers.client.page.LogPage;
import kvv.controllers.client.page.ModePage;
import kvv.controllers.client.page.FormsPage;
import kvv.controllers.client.page.SourcesPage;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Controllers implements EntryPoint {
	public void onModuleLoad() {

		RootPanel root = RootPanel.get();

		final TabPanel tabs = new TabPanel();
		tabs.setHeight("200px");

		root.add(tabs);

		ControllersPage.loadData(new CallbackAdapter<Void>() {
			@Override
			public void onSuccess(Void result) {
				tabs.add(new FormsPage(), "Объекты");
				tabs.add(new RegistersPage(), "Регистры");
				tabs.add(new ControllersPage(), "Контроллеры");
				tabs.add(new ModePage(), "Режимы работы");
				tabs.add(new SourcesPage(), "Sources");
				tabs.add(new LogPage(), "Log");

				if (ModePage.controlMode)
					tabs.add(new ConfigurationPage(), "Конфигурация");

				tabs.selectTab(0);
			}

			@Override
			public void onFailure(Throwable caught) {
				if (ModePage.controlMode)
					tabs.add(new ConfigurationPage(), "Конфигурация");
				tabs.selectTab(0);
				super.onFailure(caught);
			}
		});

	}
}
