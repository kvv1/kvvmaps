package kvv.goniometer.hw.motor;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;

import kvv.goniometer.Motor;

public abstract class SMSD implements Motor {
	private final static int BAUD = 9600;

	private SerialPort serPort;
	private InputStream inStream;
	private OutputStream outStream;

	private int pos = -1;

	protected abstract String getPort();

	protected abstract int getSpeed();

	private Collection<MotorListener> listeners = new HashSet<MotorListener>();

	@Override
	public void addListener(MotorListener listener) {
		listeners.add(listener);
	}

	private void onChange() {
		for (MotorListener listener : listeners)
			listener.onChanged();
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	private String port;

	public void init() throws Exception {
		if (!getPort().equals(port))
			close();

		try {
			CommPortIdentifier pID = CommPortIdentifier
					.getPortIdentifier(getPort());
			port = getPort();
			serPort = (SerialPort) pID.open("PortReader", 2000);
			serPort.setSerialPortParams(BAUD, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);
			serPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			inStream = serPort.getInputStream();
			outStream = serPort.getOutputStream();
		} catch (NoSuchPortException e) {
			throw new kvv.goniometer.NoSuchPortException(getPort());
		}
	}

	public void close() {
		if (serPort != null) {
			serPort.close();
			serPort = null;
			inStream = null;
			outStream = null;
			port = null;
		}
	}

	public static void main(String[] args) throws Exception {
		SMSD smsd = new SMSD() {

			@Override
			protected String getPort() {
				return "COM8";
			}

			@Override
			protected int getSpeed() {
				return 10000;
			}
		};

		smsd.moveTo(10000);
		smsd.moveTo(20000);

		while (!smsd.completed())
			Thread.sleep(100);

		smsd.close();
	}

	private String cmd(String cmd) throws Exception {

		String responseExpected = cmd + "E10*";

		if (serPort == null)
			init();
		try {
			outStream.write(cmd.getBytes());

			long t = System.currentTimeMillis();

			while (System.currentTimeMillis() - t < 200) {
				if (inStream.available() > 0)
					break;
			}

			StringBuilder sb = new StringBuilder();

			while (System.currentTimeMillis() - t < 100
					&& !sb.toString().equals(responseExpected)) {
				while (inStream.available() > 0) {
					sb.append((char) inStream.read());
					t = System.currentTimeMillis();
				}
				Thread.sleep(10);
			}

			if (!sb.toString().equals(cmd + "E10*"))
				throw new IOException("Wrong SMSD response: " + sb);

			// System.out.println(sb);

			return sb.toString();
		} catch (Exception e) {
			close();
			throw e;
		}
	}

	private Timer timer;

	private void scheduleDS(int delay, final int dist) {
		final Timer t = new Timer();
		timer = t;
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				synchronized (SMSD.this) {
					if (timer == t) {
						try {
							cmd("DS*");
						} catch (Exception e) {
						}
						timer = null;
						if (pos >= 0)
							pos += dist;
						SMSD.this.notifyAll();
						onChange();
					}
					t.cancel();
				}
			}
		}, delay);
	}

	private void stopNoExc() {
		try {
			stop();
		} catch (Exception e1) {
		}
	}

	@Override
	public synchronized void zero() throws Exception {
		try {
			stop();
			while (timer != null)
				wait();
			pos = -1;
			cmd("EN*");
			cmd("AL0*");
			cmd("SD" + getSpeed() + "*");
			cmd("DL*");
			cmd("HM*");
		} catch (Exception e) {
			stopNoExc();
			throw e;
		} finally {
			onChange();
		}
	}

	@Override
	public synchronized void zeroOK() throws Exception {
		try {
			stop();
			pos = 0;
		} finally {
			onChange();
		}
	}

	@Override
	public synchronized void moveTo(final int pos1) throws Exception {
		if (pos < 0 || pos1 < 0)
			return;

		while (timer != null)
			wait();

		if (pos == pos1)
			return;

		try {
			cmd("EN*");
			cmd("AL0*");
			cmd("SD" + getSpeed() + "*");
			if (pos1 > pos)
				cmd("DR*");
			else
				cmd("DL*");
			cmd("MV" + Math.abs(pos1 - pos) + "*");
			scheduleDS(Math.abs(pos1 - pos) * 1000 / getSpeed() + 100, pos1
					- pos);
		} catch (Exception e) {
			stopNoExc();
			throw e;
		} finally {
			onChange();
		}
	}

	@Override
	public synchronized boolean completed() {
		return timer == null;
	}

	@Override
	public synchronized void stop() throws Exception {
		try {
			pos = -1;
			cmd("SP*");
			cmd("DS*");
		} finally {
			if (timer != null)
				timer.cancel();
			timer = null;
			onChange();
		}
	}

	@Override
	public synchronized int getPos() {
		return pos;
	}

}
