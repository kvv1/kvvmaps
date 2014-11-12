package kvv.goniometer;

public interface Motor {
	
	interface MotorListener {
		void onChanged();
	}
	
	void onOff(boolean on) throws Exception;
	void zero() throws Exception;
	void zeroOK() throws Exception;
	void moveTo(int pos) throws Exception;
	boolean completed();
	void stop() throws Exception;
	int getPos();
	void addListener(MotorListener listener);
	void close();
}
