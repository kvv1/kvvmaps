package kvv.controllers.client;

import kvv.controllers.client.pages.CommandsPage;
import kvv.controllers.client.pages.ControllersPage;
import kvv.controllers.client.pages.LogPage;
import kvv.controllers.client.pages.ModePage;
import kvv.controllers.client.pages.ObjectsPage;
import kvv.controllers.client.pages.SchelulePage;
import kvv.controllers.client.pages.SourcesPage;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Controllers implements EntryPoint {
	public void onModuleLoad() {

		RootPanel root = RootPanel.get();

		TabPanel tabs = new TabPanel();
		tabs.setHeight("200px");

		root.add(tabs);

		tabs.add(new ObjectsPage(), "Объекты");
		tabs.add(new ControllersPage(), "Контроллеры");
		tabs.add(new CommandsPage(), "Команды");
		tabs.add(new SchelulePage(), "Расписание");
		tabs.add(new ModePage(), "Режимы работы");
		tabs.add(new LogPage(), "Log");
		tabs.add(new SourcesPage(), "Sources");

		tabs.selectTab(0);

	}
}
