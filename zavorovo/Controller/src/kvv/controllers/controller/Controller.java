package kvv.controllers.controller;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import kvv.controllers.register.AllRegs;
import kvv.controllers.register.RegType;
import kvv.controllers.register.RegisterUI;
import kvv.controllers.utils.ADU;
import kvv.controllers.utils.CRC16;
import kvv.controllers.utils.RTU;

public class Controller implements IController { // 9164642959 7378866

	private final String url;

	public Controller(String url) {
		this.url = url;
	}

	@Override
	public void setReg(int addr, int reg, int val) throws IOException {
		byte[] req = new byte[] { (byte) Command.CMD_MODBUS_SETREGS,
				(byte) (reg >> 8), (byte) reg, 0, 1, 2, (byte) (val >> 8),
				(byte) val };
		byte[] resp = send(url, addr, req);

		if (addr == 0)
			return;

		checkErr(Command.CMD_MODBUS_SETREGS, resp);
	}

	@Override
	public int getReg(int addr, int reg) throws IOException {
		byte[] req = new byte[] { (byte) Command.CMD_MODBUS_GETREGS,
				(byte) (reg >> 8), (byte) reg, 0, 1 };
		byte[] resp = send(url, addr, req);
		return resp[1] * 256 + (resp[2] & 0xFF);
	}

	@Override
	public int[] getRegs(int addr, int reg, int n) throws Exception {
		byte[] req = new byte[] { (byte) Command.CMD_MODBUS_GETREGS,
				(byte) (reg >> 8), (byte) reg, 0, (byte) n };
		byte[] resp = send(url, addr, req);
		checkErr(Command.CMD_MODBUS_GETREGS, resp);

		int[] res = new int[n];
		for (int i = 0; i < resp[1] / 2; i++) {
			res[i] = resp[i * 2 + 2] * 256 + (resp[i * 2 + 3] & 0xFF);
		}
		return res;
	}

	@Override
	public AllRegs getAllRegs(int addr) throws IOException {
		byte[] resp = send(url, addr,
				new byte[] { (byte) Command.CMD_GETALLREGS });
		checkErr(Command.CMD_GETALLREGS, resp);

		ArrayList<RegisterUI> ui = new ArrayList<RegisterUI>();

		int i = 1;
		int nUI = resp[i++];
		while (nUI-- > 0) {
			int reg = resp[i++] & 0xFF;
			RegType type = RegType.values()[resp[i++]];
			int nameLen = resp[i++] & 0xFF;
			StringBuilder sb = new StringBuilder();
			while (nameLen-- > 0)
				sb.append((char) resp[i++]);
			ui.add(new RegisterUI(reg, type, sb.toString()));
		}

		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (; i < resp.length - 2; i += 3) {
			int reg = resp[i];
			int val = resp[i + 1] * 256 + (resp[i + 2] & 0xFF);
			map.put(reg, val);
		}

		return new AllRegs(ui, map);
	}

	@Override
	public void upload(int addr, byte[] data) throws IOException {
		int start = 0;
		while (start < data.length) {
			int len = Math.min(32, data.length - start);
			upload(addr, start, Arrays.copyOfRange(data, start, start + len));
			start += len;
		}

		ByteArrayOutputStream req = new ByteArrayOutputStream();
		req.write((byte) Command.CMD_UPLOAD_END);

		req.write((byte) (data.length >> 8));
		req.write((byte) data.length);

		short crc = CRC16.crc16(data, 0, data.length);
		req.write((byte) (crc >> 8));
		req.write((byte) crc);

		byte[] resp = send(url, addr, req.toByteArray());
		checkErr(Command.CMD_UPLOAD_END, resp);
	}

	private void upload(int addr, int start, byte[] data) throws IOException {
		ByteArrayOutputStream req = new ByteArrayOutputStream();
		req.write((byte) Command.CMD_UPLOAD);
		req.write((byte) (start >> 8));
		req.write((byte) start);
		req.write(data);
		byte[] resp = send(url, addr, req.toByteArray());
		checkErr(Command.CMD_UPLOAD, resp);
	}

	private static void checkErr(int cmd, byte[] resp) throws IOException {
		if (resp[0] == cmd)
			return;
		ErrorCode code = ErrorCode.values()[resp[1]];
		switch (code) {
		case ERR_OK:
			return;
		default:
			throw new IOException(code.name());
		}
	}

	@Override
	public void close() {
	}

	// public static RTU send(String url, int addr, RTU rtu)
	// throws IOException {
	//
	// ADU
	//
	// }

	public static byte[] send(String url, int addr, byte[] bytes)
			throws IOException {

		ADU adu = new ADU(addr, new RTU(bytes));
		byte[] packetReceived = sendBus(url, adu.toBytes(), addr != 0);
		if (addr == 0)
			return null;
		ADU resp = ADU.fromBytes(packetReceived);
		if (resp != null && resp.addr == addr)
			return resp.rtu.toBytes();

		// error
		String msg = "wrong response from addr: " + addr;
		msg += ", cmd: ";
		for (byte b : bytes)
			msg += Integer.toHexString((int) b & 0xFF) + " ";
		msg += ", response: ";
		for (byte b : packetReceived)
			msg += Integer.toHexString((int) b & 0xFF) + " ";
		throw new IOException(msg);
	}

	public static byte[] sendBus(String url, byte[] bytes, boolean response)
			throws IOException {
		String url1 = url + "?" + (response ? "response=true&" : "") + "body=";
		String sep = "";
		for (byte b : bytes) {
			url1 += sep + ((int) b & 255);
			sep = ",";
		}

		HttpURLConnection conn = null;
		conn = (HttpURLConnection) new URL(url1).openConnection();
		conn.setRequestMethod("GET");
		conn.connect();

		String resp = new BufferedReader(new InputStreamReader(
				conn.getInputStream())).readLine();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		for (String s : resp.split(",")) {
			int a = Integer.parseInt(s.trim());
			outputStream.write(a);
		}
		return outputStream.toByteArray();

	}

}
