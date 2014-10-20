package kvv.goniometer.ui.mainpage;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import kvv.goniometer.SensorData;

@SuppressWarnings("serial")
public class DataSet extends JPanel {

	static class Data {
		float x;
		float y;
		SensorData value;

		public Data(float x, float y, SensorData value) {
			this.x = x;
			this.y = y;
			this.value = value;
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

	void addMeasure(float x, float y, SensorData d) {
		System.out.println(x + " " + y + " " + d);
		data.add(new Data(x, y, d));
		if (wnd != null)
			wnd.updateData(y);
	}
}
