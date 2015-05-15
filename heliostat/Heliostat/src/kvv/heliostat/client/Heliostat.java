package kvv.heliostat.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Heliostat extends Model implements EntryPoint {

	public void onModuleLoad() {

		TabPanel tabPanel = new TabPanel();
		tabPanel.add(new MainView(this), "Main");
		//tabPanel.add(new EnvironmentView(this), "Environment");
		tabPanel.selectTab(0);
		RootPanel.get().add(tabPanel);
		notifyViews();
	}
}
