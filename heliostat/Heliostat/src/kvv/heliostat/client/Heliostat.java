package kvv.heliostat.client;

import kvv.gwtutils.client.HorPanel;
import kvv.gwtutils.client.VertPanel;
import kvv.gwtutils.client.form.UploadForm;
import kvv.heliostat.client.model.Model;
import kvv.heliostat.client.view.AnglesTest;
import kvv.heliostat.client.view.MainView;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

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

	private TextBox uploadAddr = new TextBox();

	private Widget uploadForm = new UploadForm("Upload") {
		@Override
		public String getUrl() {
			return GWT.getModuleBaseURL() + "upload?addr="
					+ Integer.parseInt(uploadAddr.getText());
		}
	};

	public void onModuleLoad() {

		MainView mainView = new MainView(model);

		TabPanel tabPanel = new TabPanel();
		tabPanel.add(mainView, "Main");
		tabPanel.add(new AnglesTest(), "Angles");
		tabPanel.add(new VertPanel(new HorPanel(new Label("addr"), uploadAddr),
				uploadForm), "Upload");
		tabPanel.selectTab(0);
		RootPanel.get().add(tabPanel);

		// RootPanel.get().add(mainView);
		model.start();
		//model.notifyViews();
	}

}
