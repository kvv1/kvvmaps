package kvv.controllers.controller;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import kvv.controllers.register.AllRegs;
import kvv.controllers.register.RegType;
import kvv.controllers.register.RegisterUI;
import kvv.controllers.utils.CRC16;

import com.google.gson.Gson;

public class Controller implements IController {

	private final String url;

	public Controller(String url) {
		this.url = url;
	}

	@Override
	public void setReg(int addr, int reg, int val) throws IOException {
		byte[] req = new byte[] { Command.CMD_MODBUS_SETREGS,
				(byte) (reg >> 8), (byte) reg, 0, 1, 2, (byte) (val >> 8),
				(byte) val };
		send(addr, req);
	}

	@Override
	public int getReg(int addr, int reg) throws IOException {
		byte[] req = new byte[] { Command.CMD_MODBUS_GETREGS,
				(byte) (reg >> 8), (byte) reg, 0, 1 };
		byte[] resp = send(addr, req);
		return resp[1] * 256 + (resp[2] & 0xFF);
	}

	@Override
	public int[] getRegs(int addr, int reg, int n) throws IOException {
		byte[] req = new byte[] { Command.CMD_MODBUS_GETREGS,
				(byte) (reg >> 8), (byte) reg, 0, (byte) n };
		byte[] resp = send(addr, req);

		int[] res = new int[n];
		for (int i = 0; i < resp[1] / 2; i++) {
			res[i] = resp[i * 2 + 2] * 256 + (resp[i * 2 + 3] & 0xFF);
		}
		return res;
	}

	@Override
	public AllRegs getAllRegs(int addr) throws IOException {
		byte[] resp = send(addr, new byte[] { Command.CMD_GETALLREGS });

		ArrayList<RegisterUI> ui = new ArrayList<RegisterUI>();

		int i = 1;
		int nUI = resp[i++];
		while (nUI-- > 0) {
			int reg = resp[i++] & 0xFF;
			RegType type = RegType.values()[resp[i++]];
			int nameLen = resp[i++] & 0xFF;

			byte[] buf = Arrays.copyOfRange(resp, i, i + nameLen);
			i += nameLen;

			ui.add(new RegisterUI(reg, type, new String(buf, "Windows-1251")));
		}

		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (; i < resp.length - 2; i += 3) {
			int reg = resp[i];
			int val = resp[i + 1] * 256 + (resp[i + 2] & 0xFF);
			map.put(reg, val);
		}

		return new AllRegs(addr, ui, map);
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
		req.write(Command.CMD_UPLOAD_END);

		req.write((byte) (data.length >> 8));
		req.write((byte) data.length);

		short crc = CRC16.crc16(data, 0, data.length);
		req.write((byte) (crc >> 8));
		req.write((byte) crc);

		send(addr, req.toByteArray());
	}

	private void upload(int addr, int start, byte[] data) throws IOException {
		ByteArrayOutputStream req = new ByteArrayOutputStream();
		req.write(Command.CMD_UPLOAD);
		req.write((byte) (start >> 8));
		req.write((byte) start);
		req.write(data);
		send(addr, req.toByteArray());
	}

	@Override
	public void vmInit(int addr) throws IOException {
		ByteArrayOutputStream req = new ByteArrayOutputStream();
		req.write(Command.CMD_VMINIT);
		send(addr, req.toByteArray());
	}

	@Override
	public void close() {
	}

	public static class NoResponseException extends IOException {
		private static final long serialVersionUID = 1L;

		public NoResponseException(String string) {
			super(string);
		}
	}

	protected byte[] send1(int addr, byte[] bytes) throws IOException {
		String url1 = url + "/PDU?addr=" + addr + "&body="
				+ new Gson().toJson(bytes);
		HttpURLConnection conn = null;
		Reader r = null;

		try {
			conn = (HttpURLConnection) new URL(url1).openConnection();
			conn.setRequestMethod("GET");
			conn.connect();
			if (addr == 0)
				return null;
			r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			return new Gson().fromJson(r, byte[].class);
		} finally {
			if (conn != null)
				conn.disconnect();
			if (r != null)
				r.close();
		}
	}

	public byte[] send(int addr, byte[] bytes) throws IOException {
		try {
			byte[] res = send1(addr, bytes);
			if (addr != 0) {
				if (res == null)
					throw new NoResponseException("no response");
				if (res.length == 0)
					throw new IOException("response PDU len = 0");
				if (res[0] != bytes[0]) {
					if (res[0] != (bytes[0] | 0x80) || res.length < 2)
						throw new IOException("response PDU format error");
					throw new IOException("response PDU error "
							+ ErrorCode.values()[res[1]].name());
				}
			}
			return res;
		} catch (NoResponseException e) {
			throw e;
		} catch (IOException e) {
			MyLogger.log(e.getMessage());
			throw e;
		} catch (Exception e) {
			MyLogger.log(e.getMessage());
			throw new IOException(e);
		}
	}

	@Override
	public Map<Integer, Statistics> getStatistics() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearStatistics() {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) throws IOException {
		Controller c = new Controller("") {
			@Override
			protected byte[] send1(int addr, byte[] bytes) throws IOException {
				Socket s = null;
				try {
					s = new Socket("localhost", 502);
					DataOutputStream os = new DataOutputStream(
							s.getOutputStream());
					DataInputStream is = new DataInputStream(s.getInputStream());
					os.writeShort(0);
					os.writeShort(0);
					os.writeShort(bytes.length + 1); // len
					os.writeByte(addr); // addr
					os.write(bytes);
					os.flush();

					is.readShort();
					is.readShort();
					short len = is.readShort();
					int a = is.readByte();
					if (a != addr)
						throw new IOException("TCP response format error");
					byte[] res = new byte[len - 1];
					for (int i = 0; i < res.length; i++) {
						res[i] = is.readByte();
						System.out.print(res[i] + " ");
					}
					res[0] |= 0x80;
					return res;
				} finally {
					if (s != null)
						s.close();
				}
			}
		};
		c.send1(1, new byte[] { 6, 0, 4, 0x66, 0x34 });
	}
}
