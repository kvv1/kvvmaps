package kvv.controllers.controller;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Controller implements IController { // 9164642959 7378866

	private final String url;

	public Controller(String url) {
		this.url = url;
	}

	@Override
	public void setReg(int addr, int reg, int val) throws IOException {
		byte[] resp = send(url, addr,
				new byte[] { (byte) Command.CMD_SETREG.ordinal(), (byte) reg,
						(byte) (val >> 8), (byte) val });
		checkErr(resp);
	}

	@Override
	public int getReg(int addr, int reg) throws IOException {
		byte[] resp = send(url, addr,
				new byte[] { (byte) Command.CMD_GETREG.ordinal(), (byte) reg });
		checkErr(resp);
		return resp[1] * 256 + (resp[2] & 0xFF);
	}

	@Override
	public Map<Integer, Integer> getRegs(int addr) throws IOException {
		byte[] resp = send(url, addr,
				new byte[] { (byte) Command.CMD_GETREGS.ordinal() });
		checkErr(resp);
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 1; i < resp.length - 2; i += 3) {
			int reg = resp[i];
			int val = resp[i + 1] * 256 + (resp[i + 2] & 0xFF);
			map.put(reg, val);
		}
		return map;
	}

	@Override
	public void upload(int addr, int start, byte[] data) throws IOException {
		ByteArrayOutputStream req = new ByteArrayOutputStream();
		req.write((byte) Command.CMD_UPLOAD.ordinal());
		req.write((byte) (start >> 8));
		req.write((byte) start);
		req.write(data);
		byte[] resp = send(url, addr, req.toByteArray());
		checkErr(resp);
	}

	@Override
	public void upload(int addr, byte[] data) throws IOException {
		int start = 0;
		while (start < data.length) {
			int len = Math.min(32, data.length - start);
			upload(addr, start,
					Arrays.copyOfRange(data, start, start + len));
			start += len;
		}
	}


	private static void checkErr(byte[] resp) throws IOException {
		ErrorCode code = ErrorCode.values()[resp[0]];
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

	public static byte[] send(String url, int addr, byte[] bytes)
			throws IOException {
		String url1 = url + "?addr=" + addr + "&body=";
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
