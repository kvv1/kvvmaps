package kvv.heliostat.server.envir.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Controller implements IController {

	protected volatile ModbusLine modbusLine;
	
	private boolean uploading;
	

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
	public void setRegs(int addr, int reg, int... val) throws IOException {

		byte[] head = new byte[] { Command.CMD_MODBUS_SETREGS,
				(byte) (reg >> 8), (byte) reg, 0, (byte) val.length, (byte) (val.length * 2) };

		byte[] req = Arrays.copyOf(head, 6 + val.length * 2);
		for (int i = 0; i < val.length; i++) {
			req[head.length + i * 2] = (byte) (val[i] >> 8);
			req[head.length + i * 2 + 1] = (byte) val[i];
		}

		send(addr, req);
	}

	@Override
	public int getReg(int addr, int reg) throws IOException {
		byte[] req = new byte[] { Command.CMD_MODBUS_GETREGS,
				(byte) (reg >> 8), (byte) reg, 0, 1 };
		byte[] resp = send(addr, req);
		return resp[2] * 256 + (resp[3] & 0xFF);
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
	public Rule[] getRules(int addr) throws IOException {
		byte[] resp = send(addr, new byte[] { Command.MODBUS_GETRULES });

		List<Rule> rules = new ArrayList<Rule>();
		DataInputStream dis = new DataInputStream(
				new ByteArrayInputStream(resp));

		dis.readByte(); // cmd

		try {
			while (true) {
				Rule rule = new Rule();
				rule.en = dis.readByte() == 1;
				rule.srcReg = dis.readByte() & 0xFF;
				int op1 = dis.readByte();
				if (op1 < 0 || op1 >= Operation.values().length)
					op1 = 0;
				rule.op = Operation.values()[op1];
				rule.srcVal = dis.readShort();
				rule.dstReg = dis.readByte() & 0xFF;
				rule.dstVal = dis.readShort();
				rules.add(rule);
			}
		} catch (Exception e) {
		}

		return rules.toArray(new Rule[0]);
	}

	@Override
	public void setRules(int addr, Rule[] rules) throws IOException {
		int i = 0;
		for (Rule rule : rules) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeByte(Command.MODBUS_SETRULE);

			dos.writeByte(i++);

			dos.writeByte(rule.en ? 1 : 0);
			dos.writeByte(rule.srcReg);
			dos.writeByte(rule.op.ordinal());
			dos.writeShort(rule.srcVal);
			dos.writeByte(rule.dstReg);
			dos.writeShort(rule.dstVal);

			send(addr, baos.toByteArray());

		}
	}

	@Override
	public AllRegs getAllRegs(int addr) throws IOException {
		byte[] resp = send(addr, new byte[] { Command.CMD_GETALLREGS });

		int i = 1;
		int nUI = resp[i++];
		/// ui removed

		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (; i < resp.length - 2; i += 3) {
			int reg = resp[i];
			int val = resp[i + 1] * 256 + (resp[i + 2] & 0xFF);
			map.put(reg, val);
		}

		return new AllRegs(addr, map);
	}


	@Override
	public void close() {
		if (modbusLine != null)
			modbusLine.close();
	}

	public synchronized byte[] send(int addr, byte[] request) throws IOException {
		if(uploading)
			throw new IOException("UPLOADING");
		return send1(addr, request);
	}
	
	private synchronized byte[] send1(int addr, byte[] request) throws IOException {
		if(false)
			return _send(addr, request);
			
		try {
			for (byte b : request)
				System.out.print(Integer.toHexString((int) b & 0xFF) + " ");
			System.out.print(" -> ");
			byte[] res = _send(addr, request);
			for (byte b : res)
				System.out.print(Integer.toHexString((int) b & 0xFF) + " ");
			return res;
		} catch (IOException e) {
			System.out.print(e.getClass().getSimpleName() + " "
					+ e.getMessage());
			throw e;
		} finally {
			System.out.println();
		}

	}

	private byte[] _send(int addr, byte[] request) throws IOException {
		if (modbusLine == null)
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

	private static final int BLOCK_SIZE = 256;

	@Override
	public synchronized void uploadApp(int addr, byte[] image) throws IOException {
		System.out.println("uploading");
		
		uploading = true;
		try {

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
		} finally {
			uploading = false;
		}
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
