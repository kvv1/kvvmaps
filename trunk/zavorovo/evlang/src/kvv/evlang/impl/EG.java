package kvv.evlang.impl;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import kvv.controllers.controller.Controller;
import kvv.controllers.register.Register;
import kvv.controllers.register.RegisterUI;
import kvv.controllers.utils.cmdline.BooleanParam;
import kvv.controllers.utils.cmdline.CmdLine;
import kvv.controllers.utils.cmdline.IntParam;
import kvv.controllers.utils.cmdline.StringParam;
import kvv.evlang.EG1;
import kvv.evlang.ParseException;
import kvv.evlang.rt.RTContext;
import kvv.evlang.rt.TryCatchBlock;
import kvv.evlang.rt.UncaughtExceptionException;
import kvv.evlang.rt.VM;
import kvv.evlang.rt.VMStatus;

//  < ID : [ "a"-"z", "A"-"Z", "_", "\u00A0"-"\u00FF" ] ([ "a"-"z", "A"-"Z", "_", "0"-"9", "\u00A0"-"\u00FF" ])* >

public abstract class EG extends Context {

	public static PrintStream nullStream = new PrintStream(new OutputStream() {
		@Override
		public void write(int b) throws IOException {
		}
	});

	public PrintStream dumpStream = nullStream;

	public static CmdLine cmdLine = new CmdLine();
	public static StringParam url = new StringParam(cmdLine, "-url", null);
	public static IntParam addr = new IntParam(cmdLine, "-addr", null);
	public static StringParam out = new StringParam(cmdLine, "-out", null);
	public static BooleanParam sim = new BooleanParam(cmdLine, "-sim");
	public static BooleanParam dump = new BooleanParam(cmdLine, "-dump");
	public static BooleanParam run = new BooleanParam(cmdLine, "-run");

	public static void main(String args[]) throws IOException,
			UncaughtExceptionException, ParseException {
		cmdLine.parse(args);

		EG1 parser = new EG1(cmdLine.args[0]) {
			@Override
			protected ExtRegisterDescr getExtRegisterDescr(String extRegName) {
				return null;
			}
		};

		if (dump.value)
			parser.dumpStream = System.out;

		try {
			parser.parse();
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			return;
		}

		byte[] bytes = parser.dump();
		System.out.println(bytes.length + " bytes");

		if (out.value != null) {
			new File(out.value).getParentFile().mkdirs();
			OutputStream os = new FileOutputStream(out.value);
			os.write(bytes);
			os.close();
		}

		Code code = parser.gen();

		if (dump.value) {
			int i = 0;
			for (byte b : bytes) {
				System.out.printf("0x%02X, ", b);
				if (++i % 16 == 0)
					System.out.println();
			}
			System.out.println();
			Code.printHisto();

			System.out.println("Functions:");
			for (Func func : parser.funcs.funcs.values())
				func.print();

			System.out.println("Types:");
			for (Struct str : parser.structs.values()) {
				str.print();
			}
		}

		if (url.value != null && addr.value != null)
			upload(url.value, addr.value, bytes, run.value);

		if (sim.value)
			parser.run(code);
	}

	private static void upload(String url, int addr, byte[] bytes, boolean run) {

		Controller controller = null;

		try {
			controller = new Controller(url);

			if (addr < 1 || addr > 255)
				throw new IllegalArgumentException("illegal address '" + addr
						+ "'");

			controller.upload(addr, bytes);

			if (run) {
				controller.setReg(addr, Register.REG_VMONOFF, 1);
				int status = controller.getReg(addr, Register.REG_VMSTATE);
				if (status != VMStatus.VMSTATUS_RUNNING.ordinal()) {
					System.out.println(url + ", addr=" + addr + " "
							+ VMStatus.values()[status]);
				}
			}

		} catch (Exception e) {
			// e.printStackTrace();
			System.out.println(e.getClass().getSimpleName() + " "
					+ e.getMessage());
		} finally {
			if (controller != null)
				controller.close();
		}
	}

	public byte[] dump() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);

		RegisterUI[] regs = registers.getRegisterDescriptions();
		dos.writeByte(regs.length);
		for (RegisterUI reg : regs) {
			dos.writeByte(reg.reg);
			dos.writeByte(reg.type.ordinal());
			dos.writeByte(reg.text.length());
			dos.writeBytes(reg.text);
		}

		Code code = new Code(this);
		funcs.dump(code);

		dos.writeByte(funcs.size());
		for (Func f : funcs.values()) {
			dos.writeShort(f.getOff());
		}

		dos.writeByte(code.tryCatchBlocks.size());
		for (TryCatchBlock tcb : code.tryCatchBlocks) {
			dos.writeShort(tcb.from);
			dos.writeShort(tcb.to);
			dos.writeShort(tcb.handler);
		}

		dos.writeByte(constPool.size());
		for (Short s : constPool.data) {
			dos.writeShort(s);
		}

		dos.writeByte(regPool.size());
		for (Short s : regPool.data) {
			dos.writeByte(s);
		}

		for (byte b : code.code)
			dos.write(b);

		dos.close();
		return baos.toByteArray();
	}

	public void run(Code code) throws UncaughtExceptionException,
			ParseException {
		RTContext context = getRTContext(code);
		new VM(context) {
			@Override
			public void setExtReg(int addr, int reg, int value) {
			}

			@Override
			public int getExtReg(int addr, int reg) {
				return 0;
			}
		}.loop();
	}

	public RTContext getRTContext(Code code) throws ParseException {

		short rtFuncs[] = funcs.getVTable();

		RTContext.Type[] rtTypes = new RTContext.Type[structs.nextIndex];
		for (Struct str : structs.values()) {
			if (!str.isAbstract())
				rtTypes[str.index] = new RTContext.Type(str.fields.size(),
						str.getMask(), str.funcs.getVTable());
		}

		RTContext context = new RTContext(code.code, rtFuncs,
				code.tryCatchBlocks.toArray(new TryCatchBlock[0]),
				constPool.data.toArray(new Short[0]),
				regPool.data.toArray(new Short[0]),
				registers.refs.toArray(new Byte[0]), rtTypes);

		return context;
	}

}
