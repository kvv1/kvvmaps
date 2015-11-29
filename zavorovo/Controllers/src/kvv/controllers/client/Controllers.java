package kvv.controllers.client;

import kvv.controllers.client.page.ConfigurationTabPage;
import kvv.controllers.client.page.ControllersPage;
import kvv.controllers.client.page.ModePage;
import kvv.controllers.client.page.StatisticsPage;
import kvv.controllers.client.page.UnitPage;
import kvv.controllers.shared.SystemDescr;
import kvv.controllers.shared.UnitDescr;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Controllers implements EntryPoint {
	private static final ConfigurationServiceAsync configurationService = GWT
			.create(ConfigurationService.class);

	public static SystemDescr systemDescr;

	public void onModuleLoad() {

		RootPanel root = RootPanel.get();

		final TabPanel tabs = new TabPanel();
		tabs.setHeight("200px");

		root.add(tabs);

		configurationService.getSystemDescr(new AsyncCallback<SystemDescr>() {
			@Override
			public void onSuccess(SystemDescr result) {
				systemDescr = result;
				// Window.alert("x1");
				try {
					if (systemDescr.units != null)
						for (UnitDescr page : systemDescr.units)
							tabs.add(new UnitPage(page), page.name);

					tabs.add(new ControllersPage(), "Контроллеры");
					tabs.add(new ModePage(), "Режимы работы");

					if (ModePage.controlMode) {
						tabs.add(new StatisticsPage(), "Статистика");
						// tabs.add(new ConfigurationPage(), "Конф.");
						// tabs.add(new ConfigurationPageG(), "Конфигурация");
						tabs.add(new ConfigurationTabPage(), "Конфигурация");
					}
					// throw new Exception();
				} catch (Exception e) {
					e.printStackTrace();
					tabs.add(new ModePage(), "Режимы работы");
					if (ModePage.controlMode) {
						// tabs.add(new ConfigurationPage(), "Конф.");
						// tabs.add(new ConfigurationPageG(), "Конфигурация");
						tabs.add(new ConfigurationTabPage(), "Конфигурация");
					}
					tabs.selectTab(0);
					// Window.alert("zzz");
					// String st = e.getClass().getName() + ": " +
					// e.getMessage();
					// for (StackTraceElement ste : e.getStackTrace())
					// st += "\n" + ste.toString();
					// Window.alert(st);
				}
				tabs.selectTab(0);
			}

			@Override
			public void onFailure(Throwable caught) {
				// Window.alert("x2");
				tabs.add(new ModePage(), "Режимы работы");
				if (ModePage.controlMode) {
					// tabs.add(new ConfigurationPage(), "Конф.");
					// tabs.add(new ConfigurationPageG(), "Конфигурация");
					tabs.add(new ConfigurationTabPage(), "Конфигурация");
				}
				tabs.selectTab(0);
			}
		});

	}

}
