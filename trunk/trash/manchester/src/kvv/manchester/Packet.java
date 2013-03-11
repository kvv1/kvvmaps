package kvv.manchester;

public class Packet {

	public static byte[] createPacket(byte n, byte[] data) {
		byte[] packet = new byte[data.length + 1];
		packet[0] = n;
		System.arraycopy(data, 0, packet, 1, data.length);
		return packet;
	}

}
