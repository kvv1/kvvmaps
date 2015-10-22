package kvv.heliostat.server.envir.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Controller implements IController {

	protected ModbusLine modbusLine;

	public Controller() {
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
	public void close() {
		if (modbusLine != null)
			modbusLine.close();
	}

	public byte[] send(int addr, byte[] request) throws IOException {
		if(modbusLine == null)
			throw new IOException("modbusLine not set");
		
		byte[] response = modbusLine.handle(addr, request);

		if (response[0] != request[0]) {
			if ((response[0] & 0xFF) != (request[0] | 0x80)
					|| response.length < 2)
				throw new IOException("response PDU format error");
			int code = response[1] & 0xFF;
			throw new IOException("modbus code: " + code);
		}
		return response;
	}

	@Override
	public Statistics getStatistics(boolean clear) throws IOException {
		if (modbusLine == null)
			return null;
		return modbusLine.getStatistics(clear);
	}

	private void s(int addr, byte[] bytes, Integer timeout) throws IOException {
		byte[] resp = send(addr, bytes);
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

	@Override
	public void setModbusLine(ModbusLine modbusLine) {
		if (this.modbusLine != null)
			this.modbusLine.close();
		this.modbusLine = modbusLine;
	}

}
