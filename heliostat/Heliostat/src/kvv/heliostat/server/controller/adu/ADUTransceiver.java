package kvv.heliostat.server.controller.adu;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import kvv.heliostat.server.controller.BusLogger;
import kvv.heliostat.server.controller.ModbusLine;
import kvv.heliostat.server.controller.Statistics;

public class ADUTransceiver implements ModbusLine{
	private Set<Integer> failedAddrs = new HashSet<Integer>();

	public Statistics statistics = new Statistics();
	private final IPacketTransceiver packetTransceiver;

	public ADUTransceiver(IPacketTransceiver packetTransceiver) {
		this.packetTransceiver = packetTransceiver;
	}

	@Override
	public void close() {
		packetTransceiver.close();
	}

	@Override
	public synchronized byte[] handle(int addr, byte[] body) {
		PDU pdu = new PDU(body);
		ADU adu = new ADU(addr, pdu);

		int attempts = failedAddrs.contains(addr) ? 1 : 3;
		failedAddrs.remove(addr);
		for (int i = 0; i < attempts; i++) {
			byte[] res = null;
			try {
				res = packetTransceiver.sendPacket(adu.toBytes(), addr != 0);
				if (res == null)
					return null;

				ADU adu1 = ADU.fromBytes(res);
				if (adu1 == null)
					throw new IOException("wrong response ADU checksum");
				if (adu1.addr != addr)
					throw new IOException("wrong response ADU addr");

				byte[] request = adu.pdu.data;
				byte[] response = adu1.pdu.data;
				if (response[0] != request[0]) {
					if ((response[0] & 0xFF) != (request[0] | 0x80)
							|| response.length < 2)
						throw new IOException("response PDU format error");
					int code = response[1] & 0xFF;
					throw new IOException("modbus code: " + code);
				}
				BusLogger.logSuccess(addr);
				statistics.addSuccess(addr);
				return adu1.pdu.toBytes();
			} catch (Exception e) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException ee) {
				}

				statistics.addError(addr, e.getClass().getSimpleName() + " "
						+ e.getMessage());

				handleError(addr, i, body, res, e);

				if (i < attempts - 1)
					continue;

				failedAddrs.add(addr);

				return null;
			}
		}
		return null;
	}

	private void handleError(int addr, int attempt, byte[] body, byte[] res,
			Exception e) {
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

	@Override
	public Statistics getStatistics(boolean clear) {
		return statistics;
	}

}
