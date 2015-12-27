package kvv.controllers.client;

import kvv.controllers.client.page.ConfigurationTabPage;
import kvv.controllers.client.page.ControllersPage;
import kvv.controllers.client.page.StatisticsPage;
import kvv.controllers.client.page.UnitPage;
import kvv.controllers.shared.SystemDescr;
import kvv.controllers.shared.UnitDescr;
import kvv.gwtutils.client.VertPanel;
import kvv.gwtutils.client.login.LoginPanel;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
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

		root.addDomHandler(new ContextMenuHandler() {
			@Override
			public void onContextMenu(ContextMenuEvent event) {
				event.preventDefault();
				event.stopPropagation();
			}
		}, ContextMenuEvent.getType());


		
		final TabPanel tabs = new TabPanel();
		tabs.setHeight("200px");

		VertPanel vertPanel = new VertPanel(new LoginPanel(), tabs);
		root.add(vertPanel);

		configurationService.getSystemDescr(new AsyncCallback<SystemDescr>() {
			@Override
			public void onSuccess(SystemDescr result) {
				systemDescr = result;
				if (systemDescr.units != null)
					for (UnitDescr page : systemDescr.units)
						tabs.add(new UnitPage(page), page.name);

				tabs.add(new ControllersPage(), "Контроллеры");

				tabs.add(new StatisticsPage(), "Статистика");
				tabs.add(new ConfigurationTabPage(), "Конфигурация");
				tabs.add(new LogTabPage(), "log");
				tabs.selectTab(0);
			}

			@Override
			public void onFailure(Throwable caught) {
			}
		});

	}

}
