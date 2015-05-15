package kvv.heliostat.server.controller;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

	protected byte[] send1(int addr, byte[] bytes, Integer timeout)
			throws IOException {
		String url1 = url + "/PDU?addr=" + addr;
		if (timeout != null)
			url1 += "&timeout=" + timeout;
		url1 += "&body=" + new Gson().toJson(bytes);
		HttpURLConnection conn = null;

		try {
			conn = (HttpURLConnection) new URL(url1).openConnection();
			conn.setRequestMethod("GET");
			conn.connect();
			if (addr == 0)
				return null;
			Reader r = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			return new Gson().fromJson(r, byte[].class);
		} finally {
			if (conn != null)
				conn.disconnect();
		}
	}

	public byte[] send(int addr, byte[] bytes) throws IOException {
		return send(addr, bytes, null);
	}

	public byte[] send(int addr, byte[] request, Integer timeout)
			throws IOException {
		try {
			byte[] response = send1(addr, request, timeout);
			if (addr != 0) {
				if (response == null)
					throw new NoResponseException("no response");
				if (response.length == 0)
					throw new WrongResponseException("response PDU len = 0");
				if (response[0] != request[0]) {
					if ((response[0] & 0xFF) != (request[0] | 0x80)
							|| response.length < 2)
						throw new WrongResponseException(
								"response PDU format error");
					int code = response[1] & 0xFF;
					if (code >= ErrorCode.values().length)
						throw new WrongResponseException("wrong error code: "
								+ code);
					throw new ModbusCmdException(code);
				}
			}
			return response;
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
	public String getStatistics(boolean clear) throws IOException {
		String url1 = url + "/statistics";
		if (clear)
			url1 += "?clear=true";
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(url1).openConnection();
			conn.setRequestMethod("GET");
			conn.connect();
			BufferedReader r = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			return r.readLine();
		} finally {
			if (conn != null)
				conn.disconnect();
		}
	}

	public static void _main(String[] args) throws IOException {
		Controller c = new Controller("") {
			@Override
			protected byte[] send1(int addr, byte[] bytes, Integer timeout)
					throws IOException {
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
		c.send1(1, new byte[] { 6, 0, 4, 0x66, 0x34 }, null);
	}

	private void s(int addr, byte[] bytes, Integer timeout) throws IOException {
		byte[] resp = send(addr, bytes, timeout);
		for (byte b : resp)
			System.out.print(b + " ");
	}

	private byte[] getImageHex(InputStream is) throws IOException {
		List<Byte> bytes = new ArrayList<Byte>();

		int addr = 0;

		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line = rd.readLine()) != null) {
			if (line.length() == 0 || line.charAt(0) != ':')
				continue;
			int idx = 1;
			int byteCnt = Integer.parseInt(line.substring(idx, idx + 2), 16);
			idx += 2;
			int a = Integer.parseInt(line.substring(idx, idx + 4), 16);
			idx += 4;
			int cmd = Integer.parseInt(line.substring(idx, idx + 2), 16);
			idx += 2;

			if (cmd == 0) {
				if (a != addr)
					throw new IllegalArgumentException();

				for (int i = 0; i < byteCnt; i++) {
					int b = Integer.parseInt(line.substring(idx, idx + 2), 16);
					idx += 2;
					bytes.add((byte) b);
				}

				addr += byteCnt;
			}
		}
		System.out.println();
		rd.close();

		byte[] res = new byte[bytes.size()];
		int i = 0;
		for (Byte b : bytes)
			res[i++] = b;

		return res;
	}

	private int BLOCK_SIZE = 256;

	private void uploadApp(int addr, byte[] image) throws IOException {
		System.out.println("uploading");

		Integer ver = hello(addr);
		if (ver != null && ver > 0)
			s(addr, new byte[] { Command.MODBUS_BOOTLOADER }, null);

		s(addr, new byte[] { Command.MODBUS_ENABLE_APP, 0 }, null);
		int a = 0;
		while (a < image.length) {
			int l = image.length - a;
			if (l > BLOCK_SIZE)
				l = BLOCK_SIZE;

			byte[] bytes = new byte[l + 3];
			bytes[0] = Command.MODBUS_UPLOAD_APP;
			bytes[1] = (byte) (a >> 8);
			bytes[2] = (byte) a;
			System.arraycopy(image, a, bytes, 3, l);

			System.out.print("u" + a + " ");

			s(addr, bytes, 800);
			a += BLOCK_SIZE;
		}
		System.out.println("e");
		s(addr, new byte[] { Command.MODBUS_ENABLE_APP, 1 }, null);
	}

	@Override
	public void uploadAppHex(int addr, InputStream is) throws IOException {
		uploadApp(addr, getImageHex(is));
	}

	@Override
	public Integer hello(int addr) throws IOException {
		byte[] resp = send(addr, new byte[] { Command.MODBUS_HELLO });
		if (resp.length > 1)
			return (int) resp[1];
		return null;
	}

	public static void main(String[] args) throws IOException,
			InterruptedException {
		String path = "D:/googlecode/trunk/avr/v2/Release/v2.hex";
		int addr = 77;

		Controller c = new Controller("http://localhost/rs485");

		// c.s(new byte[] { 1 });
		// c.s(new byte[] { Command.MODBUS_BOOTLOADER });
		// c.s(new byte[] { Command.MODBUS_HELLO });

		c.s(addr, new byte[] { Command.MODBUS_BOOTLOADER }, null);
		c.s(addr, new byte[] { Command.MODBUS_HELLO }, null);

		c.uploadApp(addr, c.getImageHex(new FileInputStream(path)));

		Thread.sleep(2000);

		c.s(addr, new byte[] { Command.MODBUS_HELLO }, null);

		// byte[] bytes = "_:101C600067D08091A3009091A400A091A500B091AD"
		// .getBytes();
		// bytes[0] = 101;
		//
		// c.s(bytes);

		// byte[] bytes = { 101, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
	}

}
