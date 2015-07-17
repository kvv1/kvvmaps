package kvv.heliostat.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Heliostat extends Model implements EntryPoint {

	public static final String AZ_COLOR = "#00FFFF";
	public static final String AZ_COLOR_LIGHT = "#B0B0B0";
	
	public static final String ALT_COLOR = "#7CFC00";
	public static final String ALT_COLOR_LIGHT = "#B0B0B0";
	
	public static final String TRAJ_COLOR = "#A0A0A0";
	//public static final String TRAJ_COLOR_LIGHT = "#D0D0D0";
	public static final String TRAJ_COLOR_LIGHT_LIGHT = "white";
	
	public void onModuleLoad() {

		MainView mainView =  new MainView(this);
		
//		TabPanel tabPanel = new TabPanel();
//		tabPanel.add(mainView, "Main");
		//tabPanel.add(new EnvironmentView(this), "Environment");
//		tabPanel.selectTab(0);
		RootPanel.get().add(mainView);
		notifyViews();
	}
}
