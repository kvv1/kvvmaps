package kvv.aplayer;

import kvv.aplayer.chart.ChartData;
import kvv.aplayer.chart.ChartView;

import com.smartbean.androidutils.fragment.RLFragment;

public class ChartsFragment extends
		RLFragment<APActivity, IAPService> {

	public ChartsFragment() {
		super(APService.class);
	}

	@Override
	protected int getLayout() {
		return R.layout.fragment_charts;
	}

	private APServiceListener listener = new APServiceListenerAdapter() {
	};

	@Override
	protected void createUI(IAPService service) {
		service.addListener(listener);
		
		ChartView chart = (ChartView) rootView.findViewById(R.id.chart);
		
		ChartData chartData = new ChartData(100);
		chartData.add(100, 200);
		chartData.add(200, 300);
		chartData.add(100, 100);

		
		
	}

	@Override
	public void onDestroy() {
		if (conn.service != null)
			conn.service.removeListener(listener);
		super.onDestroy();
	}

}
