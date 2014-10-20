package kvv.goniometer.hw.sensor;

import java.util.Collection;
import java.util.HashSet;

import kvv.goniometer.Sensor;
import kvv.goniometer.SensorData;

public class TKA_VD_Sim implements Sensor {
	private Collection<SensorListener> listeners = new HashSet<>();

	@Override
	public void addListener(SensorListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(SensorListener listener) {
		listeners.remove(listener);
	}

	private void onChange(SensorData data) {
		for (SensorListener listener : listeners)
			listener.onChanged(data);
	}

	{
		new Thread() {
			public void run() {
				for (;;) {
					SensorData data = new SensorData();
					data.e = (int) (1000 + Math.random() * 100);
					data.x = 10;
					data.y = 20;
					data.t = 30;

					for (int i = 0; i < 20; i++) {
						data.spectrum.put(i * 100 + 1000,
								(int) (2000 + Math.random() * 1000));
					}

					onChange(data);
					try {
						sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
		}.start();
		;
	}

	@Override
	public String getError() {
		return null;
	}

	@Override
	public void init(String str) {
		// TODO Auto-generated method stub

	}

}
