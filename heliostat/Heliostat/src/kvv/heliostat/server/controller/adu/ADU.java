package kvv.heliostat.server.controller.adu;

import java.util.Arrays;

public class ADU {
	public final int addr;
	public final PDU pdu;

	public ADU(int addr, PDU pdu) {
		this.addr = addr;
		this.pdu = pdu;
	}

	public byte[] toBytes() {
		byte[] pduBytes = pdu.toBytes();
		byte[] res = new byte[pduBytes.length + 3];
		res[0] = (byte) addr;
		System.arraycopy(pduBytes, 0, res, 1, pduBytes.length);
		short sum = CRC16.crc16(res, 0, pduBytes.length + 1);
		res[res.length - 2] = (byte) sum;
		res[res.length - 1] = (byte) (sum >> 8);
		return res;
	}

	public static ADU fromBytes(byte[] data) {
		if (data.length < 3)
			return null;
		short sum = CRC16.crc16(data, 0, data.length - 2);
		if ((data[data.length - 2] & 0xFF) != (sum & 0xFF)
				|| (data[data.length - 1] & 0xFF) != ((sum >> 8) & 0xFF))
			return null;
		ADU res = new ADU(data[0], new PDU(Arrays.copyOfRange(data, 1,
				data.length - 2)));
		return res;
	}
}
