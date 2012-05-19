package kvv.controllers.client;

import kvv.controllers.client.pages.CommandsPage;
import kvv.controllers.client.pages.ControllersPage;
import kvv.controllers.client.pages.LogPage;
import kvv.controllers.client.pages.ModePage;
import kvv.controllers.client.pages.ObjectsPage;
import kvv.controllers.client.pages.SchelulePage;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Controllers implements EntryPoint {
	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	public void onModuleLoad() {

		RootPanel root = RootPanel.get();

		TabPanel tabs = new TabPanel();
		tabs.setHeight("200px");
		
		root.add(tabs);
		

		tabs.add(new ObjectsPage(controllersService), "Объекты");
		tabs.add(new ControllersPage(controllersService), "Контроллеры");
		tabs.add(new CommandsPage(controllersService), "Команды");
		tabs.add(new SchelulePage(), "Расписание");
		tabs.add(new ModePage(), "Режимы работы");
		tabs.add(new LogPage(), "Log");


		tabs.selectTab(0);

	}
}
