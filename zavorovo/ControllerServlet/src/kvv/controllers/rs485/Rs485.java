package kvv.controllers.rs485;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Rs485 {
	private final static int BAUD = 9600;

	private CommPortIdentifier pID;
	private SerialPort serPort;
	private InputStream inStream;
	private OutputStream outStream;

	private static Rs485 instance;
	
	public static synchronized Rs485 getInstance() throws Exception {
		if(instance == null) {
			Properties props = new Properties();
			FileInputStream is = new FileInputStream("c:/zavorovo/controller.properties");
			props.load(is);
			is.close();
			String com = props.getProperty("COM");
			if(com == null)
				return null;
			instance = new Rs485(com);
		}
		return instance;
	}

	public static synchronized void closeInstance() {
		if (instance != null)
			instance.close();
		instance = null;
	}

	public Rs485(String comid) throws Exception {
		pID = CommPortIdentifier.getPortIdentifier(comid);
		serPort = (SerialPort) pID.open("PortReader", 2000);
		inStream = serPort.getInputStream();
		outStream = serPort.getOutputStream();
		serPort.notifyOnDataAvailable(true);
		serPort.notifyOnOutputEmpty(true);
		serPort.addEventListener(listener);
		serPort.setSerialPortParams(BAUD, SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		serPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
		serPort.setRTS(true);
	}

	private List<Byte> inputBuffer = new ArrayList<Byte>();

	private class Listener implements SerialPortEventListener {
		long lastTime;

		@Override
		public void serialEvent(SerialPortEvent event) {
			if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
				try {
					while (inStream.available() > 0) {
						long time = System.currentTimeMillis();
						if (time - lastTime > 200) {
							// System.out.println("sz " + inputBuffer.size());
							inputBuffer.clear();
						}
						lastTime = time;
						int ch = inStream.read();
						// System.out.print(ch + " ");
						inputBuffer.add((byte) ch);
						byte byte0 = inputBuffer.get(0);
						int sz = inputBuffer.size();
						if (byte0 != 0
								&& sz == (byte0 & 255)
								|| byte0 == 0
								&& sz == ((inputBuffer.get(1) << 8) + (inputBuffer
										.get(2) & 255))) {
							synchronized (inputBuffer) {
								inputBuffer.notify();
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (event.getEventType() == SerialPortEvent.OUTPUT_BUFFER_EMPTY) {
			}
		}
	}

	private Listener listener = new Listener();

	public byte[] send(int addr, byte[] data) throws IOException {
		for (int i = 0; i < 2; i++) {
			try {
				// System.out.println("===");
				return __send(addr, data, i);
			} catch (IOException e) {
				// e.printStackTrace();
			}
		}
		return __send(addr, data, 3);
	}

	public synchronized byte[] __send(int addr, byte[] data, int attempt)
			throws IOException {
		try {
			return _send(addr, data);
		} catch (IOException e) {
			throw e;
		}
	}

	public synchronized byte[] _send(int addr, byte[] data) throws IOException {
		byte[] packet = new byte[1 + 1 + data.length + 1];
		packet[0] = (byte) packet.length;
		packet[1] = (byte) addr;
		System.arraycopy(data, 0, packet, 2, data.length);
		packet[packet.length - 1] = fletchSum(packet, 0, packet.length - 1);

		inputBuffer.clear();
		outStream.write(packet);
		outStream.flush();

		synchronized (inputBuffer) {
			if (inputBuffer.isEmpty())
				try {
					inputBuffer.wait(500);
				} catch (InterruptedException e) {
				}

			byte[] packet1 = new byte[inputBuffer.size()];
			for (int i = 0; i < inputBuffer.size(); i++)
				packet1[i] = inputBuffer.get(i);

			if (packet1.length > 2
					&& (packet1[0] & 255) == packet1.length
					&& packet1[1] == (byte) (addr | 0x80)
					&& packet1[packet1.length - 1] == fletchSum(packet1, 0,
							packet1.length - 1)) {
				byte[] resp = new byte[packet1.length - 3];
				System.arraycopy(packet1, 2, resp, 0, resp.length);
				return resp;
			} else if (packet1.length > 4
					&& packet1[0] == 0
					&& packet1.length == ((packet1[1] << 8) + (packet1[2] & 255))
					&& packet1[3] == (byte) (addr | 0x80)
					&& packet1[packet1.length - 1] == fletchSum(packet1, 0,
							packet1.length - 1)) {
				byte[] resp = new byte[packet1.length - 5];
				System.arraycopy(packet1, 4, resp, 0, resp.length);
				return resp;
			} else {
				String msg = "wrong response from addr: " + addr;
				msg += ", cmd: ";
				for (byte b : data)
					msg += Integer.toHexString((int) b & 0xFF) + " ";
				msg += ", response: ";
				for (byte b : packet1)
					msg += Integer.toHexString((int) b & 0xFF) + " ";
				throw new IOException(msg);
			}
		}
	}

	public void close() {
		serPort.close();
	}

	static private byte fletchSum(byte[] buf, int offset, int len) {
		int S = 0;
		for (; len > 0; len--) {
			byte b = buf[offset++];
			int R = b & 0xFF;
			S += R;
			S = S & 0xFF;
			if (S < R)
				S++;
		}
		// if(S = 255) S = 0;
		return (byte) S;
	}

}
