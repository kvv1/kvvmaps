package kvv.controllers.protocol.mars;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JOptionPane;

public class Mars {

	private final static int BAUD = 9600;

	private CommPortIdentifier pID;
	private SerialPort serPort;
	private InputStream inStream;
	private OutputStream outStream;

	public Mars(String comid) {
		try {
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
		} catch (Exception e) {
			System.err.println("cannot initialize COM port");
			System.exit(1);
		}
	}

	private class Listener implements SerialPortEventListener {
		@Override
		public void serialEvent(SerialPortEvent event) {
			if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
				try {
					while (inStream.available() > 0) {
						int ch = inStream.read();
						// System.out.print(" " + ch + " ");
						System.out.print((char) ch);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (event.getEventType() == SerialPortEvent.OUTPUT_BUFFER_EMPTY) {
			}
		}
	}

	private Listener listener = new Listener();

	// RS2, I96, 8N1, E96, F10, PF, N0, D4321, AFFFF, C0001, M0

	//RS2, I96, 8N1, E96, F10, PF, N0, D4321, AFFFF, C0001, M0,
	
	private static long sleepTime = 2000;
	
	public static void main(String[] args) throws Exception {
		Mars rs = new Mars("COM4");
		rs.serPort.setRTS(false);

		if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(null,
				"�������� ������� � ������� ��",
				"���������������� �����������", JOptionPane.WARNING_MESSAGE))
			return;

		
		Thread.sleep(sleepTime);

//		rs.outStream.write("00#RS4".getBytes());
//		rs.outStream.write(3);
//		System.out.println(1);
//		Thread.sleep(500);

//		rs.cmd("RS4");
		
//		rs.cmd("I96");
//		rs.cmd("E96");
//		rs.cmd("F10");
//		rs.cmd("D4321");
//		rs.cmd("AFFFF");
		rs.cmd("?");
		
		System.exit(0);
	}

	private void cmd(String cmd) throws Exception {
		outStream.write(("00#" + cmd).getBytes());
		outStream.write(3);
		System.out.println(cmd);
		Thread.sleep(sleepTime);
	}
}
