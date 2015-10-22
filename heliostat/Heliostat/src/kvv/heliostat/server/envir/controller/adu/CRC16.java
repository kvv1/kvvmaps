package kvv.heliostat.server.envir.controller.adu;

public class CRC16 {
	private final static short CRC16_INIT = -1;

	public static short crc16(byte[] buf, int offset, int len) {
		short crc_val = CRC16_INIT;

		for (int i = 0; i < len; i++) {
			byte b = buf[offset + i];
			crc_val ^= b & 0xFF;
			int j = 8;
			while (j-- > 0) {
				boolean carry = (crc_val & 0x0001) != 0;
				crc_val = (short) (crc_val << 16 >>> 17);
				if (carry)
					crc_val ^= 0xa001;
			}
		}

		return crc_val;
	}

}
