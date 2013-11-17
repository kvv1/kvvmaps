package kvv.controllers.controller;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import kvv.controllers.rs485.PacketTransceiver;
import kvv.controllers.utils.ADU;
import kvv.controllers.utils.PDU;

public class ADUTransceiver {
	private static Set<Integer> failedAddrs = new HashSet<Integer>();

	public static synchronized byte[] handle(int addr, byte[] body) {
		PDU pdu = new PDU(body);
		ADU adu = new ADU(addr, pdu);

		int attempts = failedAddrs.contains(addr) ? 1 : 3;
		failedAddrs.remove(addr);
		for (int i = 0; i < attempts; i++) {
			byte[] res = null;
			try {
				res = PacketTransceiver.getInstance().sendPacket(adu.toBytes(),
						addr != 0);
				if (res == null)
					return null;

				ADU adu1 = ADU.fromBytes(res);
				if (adu1 == null)
					throw new IOException("wrong response ADU checksum");
				if (adu1.addr != addr)
					throw new IOException("wrong response ADU addr");

				BusLogger.logSuccess(addr);
				return adu1.pdu.toBytes();
			} catch (Exception e) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException ee) {
				}

				handleError(addr, i, body, res, e);

				if (i < attempts - 1)
					continue;

				failedAddrs.add(addr);

				return null;
			}
		}
		return null;
	}

	private static void handleError(int addr, int attempt, byte[] body,
			byte[] res, Exception e) {
		String msg = attempt + " cmd: ";
		for (byte b : body)
			msg += Integer.toHexString((int) b & 0xFF) + " ";
		msg += ", response: ";
		if (res == null)
			msg += "null";
		else
			for (byte b : res)
				msg += Integer.toHexString((int) b & 0xFF) + " ";
		BusLogger.logErr(addr, msg + " " + e.getMessage());

		try {
			PrintStream ps = new PrintStream(new FileOutputStream(
					"c:/zavorovo/log/l.txt", true), true, "Windows-1251");
			e.printStackTrace(ps);
			ps.close();
		} catch (Exception e1) {
		}
	}

}
