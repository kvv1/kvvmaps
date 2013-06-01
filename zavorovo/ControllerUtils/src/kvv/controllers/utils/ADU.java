package kvv.controllers.utils;

import java.util.Arrays;

public class ADU {
	public int addr;
	public RTU rtu;
	public short crc16;

	public ADU(int addr, RTU rtu) {
		this.addr = addr;
		this.rtu = rtu;
	}

	public byte[] toBytes() {
		byte[] rtuBytes = rtu.toBytes();
		byte[] res = new byte[rtuBytes.length + 3];
		res[0] = (byte) addr;
		System.arraycopy(rtuBytes, 0, res, 1, rtuBytes.length);
		short sum = CRC16.crc16(res, 0, rtuBytes.length + 1);
		res[res.length - 2] = (byte) sum;
		res[res.length - 1] = (byte) (sum >> 8);
		return res;
	}

	public static ADU fromBytes(byte[] data) {
		short sum = CRC16.crc16(data, 0, data.length - 2);
		if ((data[data.length - 2] & 0xFF) != (sum & 0xFF)
				|| (data[data.length - 1] & 0xFF) != ((sum >> 8) & 0xFF))
			return null;
		ADU res = new ADU(data[0], new RTU(Arrays.copyOfRange(data, 1,
				data.length - 2)));
		return res;
	}
}
