package kvv.goniometer.hw.sensor;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import kvv.goniometer.Sensor;
import kvv.goniometer.SensorData;

import com.google.gson.Gson;

public class TKA_VD implements Sensor {

	//private String port;
	private SerialPort serPort;
	private InputStream inStream;

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

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

	private class Listener implements SerialPortEventListener {
		@Override
		public void serialEvent(SerialPortEvent event) {
			if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
				byte[] buf = new byte[300];
				int cnt = 0;

				try {
					while (inStream.available() > 0) {
						while (inStream.available() > 0) {
							int ch = inStream.read();
							// System.out.print(ch + " ");
							if (cnt < buf.length)
								buf[cnt++] = (byte) ch;
						}
						Thread.sleep(100);
					}

					received1(Arrays.copyOf(buf, cnt));

				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Listener listener = new Listener();

	private int getInt(byte[] data, int idx) {
		return ((data[idx] & 0xFF) << 24) | ((data[idx + 1] & 0xFF) << 16)
				| ((data[idx + 2] & 0xFF) << 8) | ((data[idx + 3] & 0xFF));

	}

	private int getShort(byte[] data, int idx) {
		return ((data[idx] & 0xFF) << 8) | ((data[idx + 1] & 0xFF));

	}

	private synchronized void received1(byte[] data) {
		SensorData sensorData = new SensorData();
		sensorData.e = getInt(data, 25);
		sensorData.x = getShort(data, 17);
		sensorData.y = getShort(data, 19);

		int n = data[29];
		for (int i = 0; i < n; i++) {
			int lambda = getShort(data, 30 + i * 2);
			int k = getShort(data, 30 + n * 2 + i * 2);
			sensorData.spectrum.put(lambda, k);
		}

		sensorData.t = getInt(data, 30 + n * 2 + n * 2);

		onChange(sensorData);
	}

	private String errMsg;

	public synchronized void init(String port) throws Exception {
		close();
		errMsg = null;

		try {
			CommPortIdentifier pID = CommPortIdentifier.getPortIdentifier(port);
			serPort = (SerialPort) pID.open("PortReader", 2000);
			serPort.setSerialPortParams(115200, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			serPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			serPort.notifyOnDataAvailable(true);
			serPort.addEventListener(listener);
			inStream = serPort.getInputStream();
		} catch (NoSuchPortException e) {
			errMsg = e.getClass().getSimpleName() + " " + port;
		} catch (Exception e) {
			errMsg = e.getClass().getSimpleName() + " " + e.getMessage();
			throw e;
		}
	}

	public synchronized void close() {
		if (serPort != null) {
			serPort.close();
			serPort = null;
//			port = null;
		}
	}

	public static void main(String[] args) throws Exception {
		TKA_VD tka_vd = new TKA_VD();
		tka_vd.init("COM9");

		tka_vd.addListener(new SensorListener() {
			@Override
			public void onChanged(SensorData data) {
				System.out.println(new Gson().toJson(data));
			}
		});

	}

	@Override
	public String getError() {
		return errMsg;
	}

}
