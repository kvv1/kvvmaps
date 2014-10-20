package kvv.goniometer;

public interface Sensor {

	public interface SensorListener {
		void onChanged(SensorData data);
	}

	void addListener(SensorListener listener);
	void removeListener(SensorListener listener);

	public String getError();
	void init(String str) throws Exception;
}
