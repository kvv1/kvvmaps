package kvv.manchester;


public class ManchesterDecoder {
	private short markerAccum;
	private byte byteAccum;
	private byte waitMarker = 1;
	private byte lastBit;

	private byte len;

	public interface DecoderListener {
		void received(byte[] data);
	}
	
	private final DecoderListener listener;
	
	public ManchesterDecoder(DecoderListener listener) {
		this.listener = listener;
	}
	
	public boolean transitionReceived(int cnt) {
		if (cnt < ManchesterEncoder.HALF_BIT_LEN / 2
				|| cnt > ManchesterEncoder.HALF_BIT_LEN * 5 / 2) {
			reset();
			return false;
		}

		if (waitMarker != 0) {
			byte longInterval = cnt > ManchesterEncoder.HALF_BIT_LEN * 3 / 2 ? (byte) 1
					: 0;
			if (markerAccum == 0xFFFFFF3F && longInterval != 0) {
				System.out.println("M");
				waitMarker = 0;
				lastBit = 1;
				len = 0;
				byteAccum = 1;
			} else {
				markerAccum <<= 1;
				markerAccum |= longInterval;
			}
			return false;
		}
		len += cnt;
		if (len > ManchesterEncoder.BIT_LEN * 5 / 4) {
			reset();
			return false;
		}
		lastBit ^= 1;
		if (len > ManchesterEncoder.HALF_BIT_LEN * 3 / 2) {
			len = 0;
			return 0 != bitReceived(lastBit);
		}
		return false;
	}

	private byte bitReceived(byte bit) {
		if ((byteAccum & 0x80) != 0) {
			byte res = byteReceived((byte) ((byteAccum << 1) | bit));
			byteAccum = 1;
			return res;
		} else {
			byteAccum <<= 1;
			byteAccum |= bit;
			return 0;
		}
	}

	byte[] buf = new byte[128];
	int bufCnt;

	private byte byteReceived(byte b) {
		System.out.println(b + " ");
		if (bufCnt == buf.length) {
			reset();
			return 0;
		}
		buf[bufCnt++] = b;
		if (bufCnt > 1 && bufCnt == buf[0] + 1) {
			if (buf[bufCnt - 1] == ManchesterEncoder.fletchSum(buf, 0,
					bufCnt - 1)) {
				byte[] pack = new byte[bufCnt - 2];
				System.arraycopy(buf, 1, pack, 0, bufCnt - 2);
				listener.received(pack);
				reset();
				return 1;
			}
			reset();
			return 0;
		}
		return 0;
	}

	private void reset() {
		waitMarker = 1;
		byteAccum = 0;
		markerAccum = 0;
		bufCnt = 0;
	}
}
