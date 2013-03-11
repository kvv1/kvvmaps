package kvv.AndroidController;

import kvv.manchester.Comm;

public class ConterollerAPI {
	private final Comm comm = new Comm();

	public boolean setOutput(int n, boolean v) {
		byte[] resp = comm.request(new byte[] { 'O', (byte) n,
				(byte) (v ? 1 : 0) });
		return resp != null;
	}

	public int getTempNum() {
		byte[] resp = comm.request(new byte[] { 'T', 'N' });
		if(resp == null)
			return 0;
		return resp[0];
	}

	public Float getTemp(int n) {
		byte[] resp = comm.request(new byte[] { 'T', (byte) n });
		if(resp == null)
			return null;
		short s = (short) ((resp[0] << 8) | (resp[1] & 0xFF));
		return ((float)s) / 10;
	}

	public void start() {
		comm.start();
	}

	public void stop() {
		comm.stop();
	}
	
}
