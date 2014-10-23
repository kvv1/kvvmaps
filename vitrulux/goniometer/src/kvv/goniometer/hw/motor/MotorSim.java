package kvv.goniometer.hw.motor;

import java.util.Collection;
import java.util.HashSet;

import kvv.goniometer.Motor;

public class MotorSim implements Motor {

	private int pos = -1;
	private boolean on;
	private Collection<MotorListener> listeners = new HashSet<MotorListener>();

	@Override
	public void zero() throws Exception {
		pos = -1;
		on = true;
		onChange();
	}

	@Override
	public void zeroOK() throws Exception {
		pos = 0;
		on = true;
		onChange();
	}

	@Override
	public void moveTo(int pos) throws Exception {
		this.pos = pos;
		on = true;
		onChange();
	}

	@Override
	public boolean completed() {
		return true;
	}

	@Override
	public void stop() throws Exception {
		pos = -1;
		on = true;
		onChange();
	}

	@Override
	public int getPos() {
		return pos;
	}

	@Override
	public void addListener(MotorListener listener) {
		listeners.add(listener);
	}

	private void onChange() {
		for (MotorListener listener : listeners)
			listener.onChanged();
	}

	@Override
	public void onOff(boolean on) {
		this.on = on;
		onChange();
	}

}
