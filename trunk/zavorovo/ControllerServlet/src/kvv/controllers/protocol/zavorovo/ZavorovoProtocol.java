package kvv.controllers.protocol.zavorovo;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import kvv.controllers.rs485.PacketSender;
import kvv.controllers.utils.FletchSum;

public class ZavorovoProtocol {
	private static ZavorovoProtocol instance;

	private final PacketSender packetSender;

	public ZavorovoProtocol(PacketSender packetSender) {
		this.packetSender = packetSender;
	}

	public static synchronized ZavorovoProtocol getInstance() throws Exception {
		if (instance == null) {
			Properties props = new Properties();
			FileInputStream is = new FileInputStream(
					"c:/zavorovo/controller.properties");
			props.load(is);
			is.close();
			String com = props.getProperty("COM");
			if (com == null)
				return null;
			instance = new ZavorovoProtocol(new PacketSender(com));
		}
		return instance;
	}

	public static synchronized void closeInstance() {
		if (instance != null)
			instance.close();
		instance = null;
	}

	public synchronized byte[] send(int addr, byte[] data) throws Exception {
		for (int i = 0; i < 2; i++) {
			try {
				return _send(addr, data);
			} catch (IOException e) {
			}
		}
		return _send(addr, data);
	}

	private synchronized byte[] _send(int addr, byte[] data) throws IOException {
		byte[] packet = new byte[1 + 1 + data.length + 1];
		packet[0] = (byte) packet.length;
		packet[1] = (byte) addr;
		System.arraycopy(data, 0, packet, 2, data.length);
		packet[packet.length - 1] = FletchSum.fletchSum(packet, 0,
				packet.length - 1);

		byte[] packet1 = packetSender.sendPacket(packet);

		if (packet1.length > 2
				&& (packet1[0] & 255) == packet1.length
				&& packet1[1] == (byte) (addr | 0x80)
				&& packet1[packet1.length - 1] == FletchSum.fletchSum(packet1,
						0, packet1.length - 1)) {
			byte[] resp = new byte[packet1.length - 3];
			System.arraycopy(packet1, 2, resp, 0, resp.length);
			return resp;
		} else if (packet1.length > 4
				&& packet1[0] == 0
				&& packet1.length == ((packet1[1] << 8) + (packet1[2] & 255))
				&& packet1[3] == (byte) (addr | 0x80)
				&& packet1[packet1.length - 1] == FletchSum.fletchSum(packet1,
						0, packet1.length - 1)) {
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

	public void close() {
		packetSender.close();
	}

}
