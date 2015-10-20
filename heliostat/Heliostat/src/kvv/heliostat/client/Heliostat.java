package kvv.heliostat.client;

import kvv.heliostat.client.model.Model;
import kvv.heliostat.client.view.AnglesTest;
import kvv.heliostat.client.view.MainView;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Heliostat implements EntryPoint {

	public static final String AZ_COLOR = "#00FFFF";
	public static final String AZ_COLOR_LIGHT = "#B0B0B0";

	public static final String ALT_COLOR = "#7CFC00";
	public static final String ALT_COLOR_LIGHT = "#B0B0B0";

	public static final String TRAJ_COLOR = "#A0A0A0";
	// public static final String TRAJ_COLOR_LIGHT = "#D0D0D0";
	public static final String TRAJ_COLOR_LIGHT_LIGHT = "white";

	public static final String REFRESH_PERIOD = "RefreshPeriod";
	
	private final Model model = new Model();
	
	public void onModuleLoad() {

		MainView mainView = new MainView(model);

		TabPanel tabPanel = new TabPanel();
		tabPanel.add(mainView, "Main");
		tabPanel.add(new AnglesTest(), "Angles");
		tabPanel.selectTab(0);
		RootPanel.get().add(tabPanel);

		//RootPanel.get().add(mainView);
		model.start();
		model.notifyViews();
	}
	
}
