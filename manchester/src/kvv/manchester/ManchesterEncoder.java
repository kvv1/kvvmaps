package kvv.manchester;

public class ManchesterEncoder {
	public static final int HALF_BIT_LEN = 4;
	public static final int BIT_LEN = HALF_BIT_LEN * 2;

	public static byte[] encode(byte[] data) {
		// 0 0 AA 55 L <N data> CS
		byte[] packet = new byte[data.length + 6];
		packet[0] = 0;
		packet[1] = 0;
		packet[2] = (byte) 0xAA;
		packet[3] = 0x55;
		packet[4] = (byte) (data.length + 1);
		System.arraycopy(data, 0, packet, 5, data.length);
		packet[packet.length - 1] = ManchesterEncoder.fletchSum(packet, 4,
				data.length + 1);

		return encodeRaw(packet);
	}

	public static byte[] encodeRaw(byte[] data) {
		byte[] samples = new byte[data.length * BIT_LEN * 8];
		int cnt = 0;

		for (byte b : data) {
			cnt += encodeByte(b, samples, cnt);
		}

//		for(int i = 0; i < 200; i++) {
//			if(data[i] == 0)
//		}
		
		return samples;
	}

	private static int encodeByte(byte b, byte[] buf, int offset) {
		for (int i = 0; i < 8; i++) {
			offset += encodeBit(b < 0, buf, offset);
			b <<= 1;
		}
		return BIT_LEN * 8;
	}

	private static int encodeBit(boolean bit, byte[] buf, int offset) {
		byte b0, b1;
		if (bit) {
			b0 = 10;
			b1 = -10;
		} else {
			b0 = -10;
			b1 = 10;
		}

		for (int k = 0; k < HALF_BIT_LEN; k++)
			buf[offset++] = b0;
		for (int k = 0; k < HALF_BIT_LEN; k++)
			buf[offset++] = b1;

		return BIT_LEN;
	}

	static public byte fletchSum(byte[] buf, int offset, int len) {
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
