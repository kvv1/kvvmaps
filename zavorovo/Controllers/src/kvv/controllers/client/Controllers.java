package kvv.controllers.client;

import kvv.controllers.client.page.ConfigurationPage;
import kvv.controllers.client.page.ControllersPage;
import kvv.controllers.client.page.LogPage;
import kvv.controllers.client.page.ModePage;
import kvv.controllers.client.page.SourcesPage;
import kvv.controllers.client.page.UnitPage;
import kvv.controllers.shared.PageDescr;

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

		// Window.alert("xx");
		ControllersPage.loadData(new CallbackAdapter<Void>() {
			@Override
			public void onSuccess(Void result) {
				// Window.alert("x1");
				try {
					if (ControllersPage.pages != null)
						for (PageDescr page : ControllersPage.pages)
							tabs.add(new UnitPage(page), page.name);

					tabs.add(new ControllersPage(), "Контроллеры");
					tabs.add(new ModePage(), "Режимы работы");
					tabs.add(new SourcesPage(), "Sources");
					tabs.add(new LogPage(), "Log");

					if (ModePage.controlMode)
						tabs.add(new ConfigurationPage(), "Конфигурация");
					// throw new Exception();
				} catch (Exception e) {
					if (ModePage.controlMode)
						tabs.add(new ConfigurationPage(), "Конфигурация");
					tabs.selectTab(0);
					// Window.alert("zzz");
					String st = e.getClass().getName() + ": " + e.getMessage();
					for (StackTraceElement ste : e.getStackTrace())
						st += "\n" + ste.toString();
					// Window.alert(st);
				}
				tabs.selectTab(0);
			}

			@Override
			public void onFailure(Throwable caught) {
				// Window.alert("x2");
				if (ModePage.controlMode)
					tabs.add(new ConfigurationPage(), "Конфигурация");
				tabs.selectTab(0);
				super.onFailure(caught);
			}
		});

	}
}
