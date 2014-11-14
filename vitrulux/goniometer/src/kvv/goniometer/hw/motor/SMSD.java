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

import javax.swing.SwingUtilities;

import kvv.goniometer.Motor;
import kvv.goniometer.Props;
import kvv.goniometer.ui.props.Prop;

public abstract class SMSD implements Motor {
	private final static int BAUD = 9600;

	private SerialPort serPort;
	private InputStream inStream;
	private OutputStream outStream;
	private final Props props;

	private int pos = -1;

	protected abstract String getPort();

	protected abstract int getSpeed();

	private Collection<MotorListener> listeners = new HashSet<MotorListener>();

	public SMSD(Props props) {
		this.props = props;
	}

	@Override
	public synchronized void addListener(MotorListener listener) {
		listeners.add(listener);
	}

	private void onChange() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				synchronized (SMSD.this) {
					for (MotorListener listener : listeners)
						listener.onChanged();
				}
			}
		});
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	private String port;

	public void init() throws Exception {
		if (isSim())
			return;

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
			cmd("AL0*");
			cmd("SD" + getSpeed() + "*");
		} catch (NoSuchPortException e) {
			throw new kvv.goniometer.NoSuchPortException(getPort());
		}
	}

	@Override
	public synchronized void close() {
		if (serPort != null) {
			serPort.close();
			serPort = null;
			inStream = null;
			outStream = null;
			port = null;
		}
	}

	private String cmd(String cmd) throws Exception {

		System.out.println("> " + cmd);

		String responseExpected = cmd + "E10*";

		if (isSim()) {
			System.out.println("< " + responseExpected);
			return responseExpected;
		}

		if (serPort == null)
			init();
		try {
			outStream.write(cmd.getBytes());

			long t = System.currentTimeMillis();

			while (System.currentTimeMillis() - t < 300) {
				if (inStream.available() > 0)
					break;
			}

			StringBuilder sb = new StringBuilder();

			while (System.currentTimeMillis() - t < 200
					&& !sb.toString().equals(responseExpected)) {
				while (inStream.available() > 0) {
					sb.append((char) inStream.read());
					t = System.currentTimeMillis();
				}
				Thread.sleep(10);
			}

			System.out.println("< " + sb);

			if (!sb.toString().equals(responseExpected))
				throw new IOException("Wrong SMSD response: " + sb);

			System.out.println(sb);
			return sb.toString();
		} catch (Exception e) {
			System.out.println(e.getClass().getSimpleName() + " "
					+ e.getMessage());
			close();
			throw e;
		}
	}

	private Timer timer;

	private void scheduleTimer(int delay, final int dist) {
		final Timer t = new Timer();
		timer = t;
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				synchronized (SMSD.this) {
					if (timer == t) {
						timer = null;
						System.out.println("timer:");
						if (isAutoOff()) {
							try {
								onOff(false);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						if (pos >= 0)
							pos += dist;
						System.out.println("pos -> " + pos);
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
		System.out.println("zero");
		try {
			stop();
			while (timer != null)
				wait();
			pos = -1;
			cmd("EN*");
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
		System.out.println("zeroOK");
		try {
			stop();
			pos = 0;
		} finally {
			onChange();
		}
	}

	@Override
	public synchronized void moveTo(final int pos1) throws Exception {
		System.out.println("moveTo " + pos1);

		if (pos < 0 || pos1 < 0)
			return;

		while (timer != null)
			wait();

		if (pos == pos1)
			return;

		try {
			cmd("EN*");
			if (pos1 > pos)
				cmd("DR*");
			else
				cmd("DL*");
			cmd("MV" + Math.abs(pos1 - pos) + "*");
			if (isSim())
				scheduleTimer(getAdditioalDelay(), pos1 - pos);
			else
				scheduleTimer(Math.abs(pos1 - pos) * 1000 / getSpeed()
						+ getAdditioalDelay(), pos1 - pos);
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
		System.out.println("stop");
		try {
			pos = -1;
			cmd("SP*");
			onOff(false);
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

	@Override
	public synchronized void onOff(boolean on) throws Exception {
		if (on) {
			System.out.println("on");
			cmd("EN*");
		} else {
			System.out.println("off");
			cmd("DS*");
		}
		onChange();
	}

	private boolean isAutoOff() {
		return props.get(Prop.AUTO_MOTOR_OFF).equals("true");
	}

	private int getAdditioalDelay() {
		return props.getInt(Prop.MOTOR_ADDITIONAL_DELAY);
	}

	private boolean isSim() {
		return props.get(Prop.SIM_MOTORS).equals("true");
	}
}
