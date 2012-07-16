package kvv.controllers.server.rs485;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import kvv.controllers.shared.Constants;

public class Controller implements IController { // 9164642959 7378866

	private static final int ERR_OK = 0;
	private static final int ERR_UNKNOWN_CMD = 1;
	private static final int ERR_INVALID_PORT_NUM = 2;

	private static final int CMD_SETREG = 1;
	private static final int CMD_GETREG = 2;
	private static final int CMD_GETREGS = 3;

	private final Transceiver rs;

	public Controller(Transceiver transciever) {
		this.rs = transciever;
	}

	@Override
	public void setReg(int addr, int reg, int val) throws IOException {
		byte[] resp = rs.send(addr, new byte[] { CMD_SETREG, (byte) reg,
				(byte) (val >> 8), (byte) val });
		checkErr(resp);
	}

	@Override
	public int getReg(int addr, int reg) throws IOException {
		byte[] resp = rs.send(addr, new byte[] { CMD_GETREG, (byte) reg });
		checkErr(resp);
		return resp[1] * 256 + (resp[2] & 0xFF);
	}

	@Override
	public Map<Integer, Integer> getRegs(int addr) throws IOException {
		byte[] resp = rs.send(addr, new byte[] { CMD_GETREGS });
		checkErr(resp);
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 1; i < resp.length - 2; i += 3) {
			int reg = resp[i];
			int val = resp[i + 1] * 256 + (resp[i + 2] & 0xFF);
			map.put(reg, val);
		}
		return map;
	}

	private static void checkErr(byte[] resp) throws IOException {
		switch (resp[0]) {
		case ERR_OK:
			return;
		case ERR_UNKNOWN_CMD:
			throw new IOException("ERR_UNKNOWN_CMD");
		case ERR_INVALID_PORT_NUM:
			throw new IOException("ERR_INVALID_PORT_NUM");
		default:
			throw new IOException("unknown error");
		}
	}

	public static void main(String[] args) throws Exception {

		Controller controller = new Controller(new Rs485("COM5"));
		Thread.sleep(1000);

		try {
			System.out.println("addr set ok");
			controller.setReg(1, Constants.REG_RELAYS, 9);
			System.out.println("outs="
					+ (int) controller.getReg(1, Constants.REG_RELAYS));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// System.exit(0);

		for (;;) {
			try {
				int t = controller.getReg(1, Constants.REG_TEMP);
				System.out.println(t);
			} catch (IOException e) {
				System.err.println("error " + e.getMessage());
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public void close() {
		rs.close();
	}

}
