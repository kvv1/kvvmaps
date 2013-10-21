package kvv.evlang.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import kvv.controllers.controller.Controller;
import kvv.controllers.register.Register;
import kvv.controllers.utils.cmdline.BooleanParam;
import kvv.controllers.utils.cmdline.CmdLine;
import kvv.controllers.utils.cmdline.IntParam;
import kvv.controllers.utils.cmdline.StringParam;
import kvv.evlang.EG1;
import kvv.evlang.ParseException;
import kvv.evlang.rt.VMStatus;

//  < ID : [ "a"-"z", "A"-"Z", "_", "\u00A0"-"\u00FF" ] ([ "a"-"z", "A"-"Z", "_", "0"-"9", "\u00A0"-"\u00FF" ])* >

public abstract class EG extends Context {

	public static CmdLine cmdLine = new CmdLine();
	public static StringParam url = new StringParam(cmdLine, "-url", null);
	public static IntParam addr = new IntParam(cmdLine, "-addr", null);
	public static StringParam out = new StringParam(cmdLine, "-out", null);
	public static BooleanParam sim = new BooleanParam(cmdLine, "-sim");
	public static BooleanParam dump = new BooleanParam(cmdLine, "-dump");
	public static BooleanParam run = new BooleanParam(cmdLine, "-run");

	public static void main(String args[]) throws IOException {
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

		if (dump.value) {
			int i = 0;
			for (byte b : bytes) {
				System.out.printf("0x%02X, ", b);
				if (++i % 16 == 0)
					System.out.println();
			}
			System.out.println();
			Code.printHisto();
		}

		if (url.value != null && addr.value != null)
			upload(url.value, addr.value, bytes, run.value);

		if (sim.value)
			parser.run();
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

}
