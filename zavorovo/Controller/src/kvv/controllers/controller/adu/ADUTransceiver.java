package kvv.controllers.controller.adu;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kvv.controller.register.Statistics;
import kvv.controllers.controller.BusLogger;
import kvv.controllers.controller.ModbusLine;

public class ADUTransceiver implements ModbusLine {
	private Set<Integer> failedAddrs = new HashSet<Integer>();

	public Statistics statistics = new Statistics();
	protected final IPacketTransceiver packetTransceiver;

	private final LinkedList<String> log = new LinkedList<>();

	private final Map<Integer, Integer> timeouts = new HashMap<>();

	@Override
	public void setTimeout(int addr, int timeout) {
		timeouts.put(addr, timeout);
	}

	public ADUTransceiver(IPacketTransceiver packetTransceiver) {
		this.packetTransceiver = packetTransceiver;
	}

	@Override
	public void close() {
		packetTransceiver.close();
	}

	@Override
	public synchronized byte[] handle(int addr, byte[] body, Integer timeout)
			throws IOException {
		PDU pdu = new PDU(body);
		ADU adu = new ADU(addr, pdu);

		int attempts = failedAddrs.contains(addr) ? 1 : 3;
		failedAddrs.remove(addr);
		for (int i = 0; i < attempts; i++) {
			byte[] res = null;
			try {
				if (timeout == null)
					timeout = timeouts.get(addr);
				if (timeout == null)
					timeout = 100;
				res = packetTransceiver.sendPacket(adu.toBytes(), addr != 0,
						timeout);
				if (addr == 0)
					return null;

				addLog(adu.toBytes(), res);

				if (res == null)
					throw new IOException("no response");

				ADU adu1 = ADU.fromBytes(res);
				if (adu1 == null)
					throw new IOException("wrong response ADU checksum");
				if (adu1.addr != addr)
					throw new IOException("wrong response ADU addr");
				BusLogger.logSuccess(addr);
				statistics.addSuccess(addr);
				return adu1.pdu.toBytes();
			} catch (IOException e) {
				addLog(e.getClass().getSimpleName() + " " + e.getMessage());

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

				throw e;
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
	public synchronized Statistics getStatistics(boolean clear) {
		Statistics statistics = this.statistics;
		if (clear)
			this.statistics = new Statistics();
		return statistics;
	}

	@Override
	public synchronized List<String> getLog() {
		return new ArrayList<String>(log);
	}

	private void addLog(byte[] req, byte[] resp) {
		StringBuilder sb = new StringBuilder();
		for (byte b : req)
			sb.append(b + " ");
		sb.append("\n ");
		if (resp != null)
			for (byte b : resp)
				sb.append(b + " ");
		else
			sb.append("null");

		addLog(sb.toString());
	}

	private void addLog(String string) {
		if (log.size() > 100)
			log.removeFirst();
		log.add(string);
	}

}
