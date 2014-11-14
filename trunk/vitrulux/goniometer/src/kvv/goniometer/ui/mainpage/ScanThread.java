package kvv.goniometer.ui.mainpage;

import javax.swing.SwingUtilities;

import kvv.goniometer.Motor;
import kvv.goniometer.Sensor;
import kvv.goniometer.Sensor.SensorListener;
import kvv.goniometer.SensorData;

public abstract class ScanThread extends Thread {
	int cnt;
	volatile SensorData data;

	private final ScanParams scanParamsPrim;
	private final ScanParams scanParamsSec;

	private final Motor motorPrim;
	private final Motor motorSec;

	private final Sensor sensor;
	private final SensorPrams sensorPrams;

	public ScanThread(Motor motorPrim, Motor motorSec, ScanParams scanParamsPrim,
			ScanParams scanParamsSec, Sensor sensor, SensorPrams sensorPrams) {
		this.motorPrim = motorPrim;
		this.motorSec = motorSec;
		this.scanParamsPrim = scanParamsPrim;
		this.scanParamsSec = scanParamsSec;
		this.sensorPrams = sensorPrams;
		this.sensor = sensor;
	}

	protected abstract void onData(float prim, float sec, SensorData data);

	protected abstract void onErr(Exception e);

	protected abstract void onFinished();

	@Override
	public void run() {
		SensorListener listener = new SensorListener() {
			@Override
			public synchronized void onChanged(SensorData data) {
				if (cnt < sensorPrams.getSensorDelay()) {
					cnt++;
				} else {
					ScanThread.this.data = data;
				}
			}
		};

		sensor.addListener(listener);

		try {
			for (float prim = scanParamsPrim.getDegStart(); prim <= scanParamsPrim
					.getDegEnd(); prim += scanParamsPrim.getDegStep()) {
				new Interruptor() {
					@Override
					protected boolean readyToContinue() {
						return motorPrim.completed();
					}
				}.pause();
				motorPrim
						.moveTo((int) ((prim - scanParamsPrim.getDegStart())
								* scanParamsPrim.getRange() / (scanParamsPrim
								.getDegEnd() - scanParamsPrim.getDegStart())));
				for (float sec = scanParamsSec.getDegStart(); sec <= scanParamsSec
						.getDegEnd(); sec += scanParamsSec.getDegStep()) {
					new Interruptor() {
						@Override
						protected boolean readyToContinue() {
							return motorSec.completed();
						}
					}.pause();
					motorSec.moveTo((int) ((sec - scanParamsSec
							.getDegStart()) * scanParamsSec.getRange() / (scanParamsSec
							.getDegEnd() - scanParamsSec.getDegStart())));
					new Interruptor() {
						@Override
						protected boolean readyToContinue() {
							return motorPrim.completed()
									&& motorSec.completed();
						}
					}.pause();

					synchronized (listener) {
						cnt = 0;
						data = null;
					}

					new Interruptor() {
						@Override
						protected boolean readyToContinue() {
							return data != null;
						}
					}.pause();

					final float prim1 = prim;
					final float sec1 = sec;

					final SensorData d1 = data;

					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							onData(prim1, sec1, d1);
						}
					});

				}
			}

			motorSec.moveTo(0);
			motorPrim.moveTo(0);

		} catch (final Exception e) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					onErr(e);
				}
			});
		} finally {
			try {
				new Interruptor() {
					@Override
					protected boolean readyToContinue() {
						return motorPrim.completed()
								&& motorSec.completed();
					}
				}.pause();
				try {
					motorSec.onOff(false);
				} catch (Exception e) {
				}
				try {
					motorPrim.onOff(false);
				} catch (Exception e) {
				}
			} catch (final InterruptedException e1) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						onErr(e1);
					}
				});
			}
			sensor.removeListener(listener);

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					onFinished();
				}
			});
		}
	}

	abstract class Interruptor {
		protected abstract boolean readyToContinue();

		public void pause() throws InterruptedException {
			do {
				if (motorPrim.getPos() < 0 || motorSec.getPos() < 0)
					throw new InterruptedException("операция прервана");
			} while (!readyToContinue());
		}

	}

}