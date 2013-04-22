package kvv.controllers.protocol.zavorovo;

import java.io.IOException;

import kvv.controllers.rs485.PacketTransceiver;
import kvv.controllers.utils.FletchSum;

public class ZavorovoProtocol {
	public static synchronized byte[] send(int addr, byte[] data)
			throws IOException {
		for (int i = 0; i < 2; i++) {
			try {
				return _send(addr, data);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
		try {
			return _send(addr, data);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	private static synchronized byte[] _send(int addr, byte[] bytes)
			throws Exception {
		byte[] packet = new byte[1 + 1 + bytes.length + 1];
		packet[0] = (byte) packet.length;
		packet[1] = (byte) addr;
		System.arraycopy(bytes, 0, packet, 2, bytes.length);
		packet[packet.length - 1] = FletchSum.fletchSum(packet, 0,
				packet.length - 1);

		byte[] packet1 = PacketTransceiver.getInstance().sendPacket(packet,
				addr != 0);

		if (addr == 0)
			return null;

		if (packet1.length > 2
				&& (packet1[0] & 255) == packet1.length
				&& packet1[1] == (byte) (addr | 0x80)
				&& packet1[packet1.length - 1] == FletchSum.fletchSum(packet1,
						0, packet1.length - 1)) {
			byte[] resp = new byte[packet1.length - 3];
			System.arraycopy(packet1, 2, resp, 0, resp.length);
			return resp;
		} else {
			String msg = "wrong response from addr: " + addr;
			msg += ", cmd: ";
			for (byte b : bytes)
				msg += Integer.toHexString((int) b & 0xFF) + " ";
			msg += ", response: ";
			for (byte b : packet1)
				msg += Integer.toHexString((int) b & 0xFF) + " ";
			throw new IOException(msg);
		}
	}
}
