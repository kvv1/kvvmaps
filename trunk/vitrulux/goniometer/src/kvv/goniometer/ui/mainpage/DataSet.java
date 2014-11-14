package kvv.goniometer.ui.mainpage;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import kvv.goniometer.SensorData;

@SuppressWarnings("serial")
public class DataSet extends JPanel {

	public static class Data {
		public final float x;
		public final float y;
		public final float R;
		SensorData value;

		public Data(float x, float y, float R, SensorData value) {
			this.x = x;
			this.y = y;
			this.R = R;
			this.value = value;
		}

		public float getPrim(DIR dir) {
			return dir == DIR.AZIMUTH ? x : y;
		}

		public float getSec(DIR dir) {
			return dir == DIR.AZIMUTH ? y : x;
		}
	}

	private List<Data> data = new ArrayList<>();

	private IMainView wnd;

	public void setWnd(IMainView wnd) {
		this.wnd = wnd;
	}

	public List<Data> getData() {
		return data;
	}

	public void clear() {
		data.clear();
		if (wnd != null)
			wnd.updateData(null);
	}

	void addMeasure(float x, float y, float R, SensorData d) {
		// System.out.println(x + " " + y + " " + d);
		Data dd = new Data(x, y, R, d);
		data.add(dd);
		if (wnd != null)
			wnd.updateData(dd);
	}
}
