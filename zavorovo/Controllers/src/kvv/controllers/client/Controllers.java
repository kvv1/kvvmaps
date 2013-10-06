package kvv.controllers.client;

import kvv.controllers.client.page.ControllersDescrPage;
import kvv.controllers.client.page.ControllersPage;
import kvv.controllers.client.page.GSchedulePage;
import kvv.controllers.client.page.LogPage;
import kvv.controllers.client.page.ModePage;
import kvv.controllers.client.page.ObjectsPage;
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
				tabs.add(new ObjectsPage(), "Объекты");
				tabs.add(new ControllersPage(), "Контроллеры");
				//tabs.add(new SchedulePage(), "Расписание");
				tabs.add(new GSchedulePage(), "ГРасписание");
				tabs.add(new ModePage(), "Режимы работы");
				tabs.add(new SourcesPage(), "Sources");
				tabs.add(new LogPage(), "Log");

				if (ModePage.check())
					tabs.add(new ControllersDescrPage(), "Конфигурация");

				tabs.selectTab(0);
			}

			@Override
			public void onFailure(Throwable caught) {
				if (ModePage.check())
					tabs.add(new ControllersDescrPage(), "Конфигурация");
				tabs.selectTab(0);
				super.onFailure(caught);
			}
		});

	}
}
