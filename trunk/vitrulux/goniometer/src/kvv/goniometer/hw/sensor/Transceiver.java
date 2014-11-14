package kvv.goniometer.hw.sensor;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public abstract class Transceiver {

	// private String port;
	private SerialPort serPort;
	private InputStream inStream;
	private OutputStream outStream;

	protected abstract void received(byte[] data);

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
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
						Thread.sleep(200);
					}

					final byte[] received = Arrays.copyOf(buf, cnt);
					received(received);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Listener listener = new Listener();

	private String errMsg;

	public void init(String port, int baudRate, int dataBits, int stopBits,
			int parity, int flowControl) throws Exception {
		close();
		errMsg = null;

		try {
			CommPortIdentifier pID = CommPortIdentifier.getPortIdentifier(port);
			serPort = (SerialPort) pID.open("PortReader", 2000);
			serPort.setSerialPortParams(baudRate, dataBits, stopBits, parity);
			serPort.setFlowControlMode(flowControl);
			serPort.notifyOnDataAvailable(true);
			serPort.addEventListener(listener);
			inStream = serPort.getInputStream();
			outStream = serPort.getOutputStream();
		} catch (NoSuchPortException e) {
			errMsg = e.getClass().getSimpleName() + " " + port;
		} catch (Exception e) {
			errMsg = e.getClass().getSimpleName() + " " + e.getMessage();
			throw e;
		}
	}

	public void send(byte[] bytes) throws IOException {
		outStream.write(bytes);
	}

	public void close() {
		if (serPort != null) {
			serPort.close();
			serPort = null;
		}
	}

	public String getError() {
		return errMsg;
	}

}
