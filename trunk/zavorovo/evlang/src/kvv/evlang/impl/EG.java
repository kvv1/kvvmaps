package kvv.evlang.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import kvv.controllers.controller.Controller;
import kvv.controllers.register.Register;
import kvv.controllers.utils.cmdline.BooleanParam;
import kvv.controllers.utils.cmdline.CmdLine;
import kvv.controllers.utils.cmdline.StringParam;
import kvv.evlang.EG1;
import kvv.evlang.ParseException;
import kvv.evlang.Token;
import kvv.evlang.rt.VMStatus;

public class EG extends Context {

	public static CmdLine cmdLine = new CmdLine();
	public static StringParam url = new StringParam(cmdLine, "-url", null);
	public static StringParam addrs = new StringParam(cmdLine, "-addrs", null);
	public static StringParam out = new StringParam(cmdLine, "-out", null);
	public static BooleanParam sim = new BooleanParam(cmdLine, "-sim");
	public static BooleanParam dump = new BooleanParam(cmdLine, "-dump");
	public static BooleanParam norun = new BooleanParam(cmdLine, "-norun");

	public static PrintStream nullStream = new PrintStream(new OutputStream() {
		@Override
		public void write(int b) throws IOException {
		}
	});

	public static PrintStream dumpStream = nullStream;

	public static void main(String args[]) throws IOException {
		for (BC bc : BC.values())
			System.out.print(bc.name() + ",");
		System.out.println();

		cmdLine.parse(args);

		if (dump.value)
			dumpStream = System.out;

		EG1 parser = new EG1(new ELReader(new FileReader(cmdLine.args[0])));

		try {
			parser.parse();
		} catch (ParseException e) {
			Token t = parser.token;
			System.err.println(" line " + t.beginLine + " : " + e.getMessage());
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

		if (url.value != null && addrs.value != null)
			upload(url.value, addrs.value.split(";"), bytes, !norun.value);

		if (sim.value)
			parser.run();
	}

	private static void upload(String url, String[] addrs, byte[] bytes,
			boolean run) {
		for (String addr : addrs) {
			upload(url, addr, bytes, run);
		}
	}

	private static void upload(String url, String addr, byte[] bytes,
			boolean run) {

		Controller controller = null;

		try {
			controller = new Controller(url);

			int a;
			try {
				a = Integer.parseInt(addr);
				if (a < 1 || a > 255)
					throw new Exception();
			} catch (Exception e) {
				throw new IllegalArgumentException("illegal address '" + addr
						+ "'");
			}

			controller.upload(a, bytes);

			if (run) {
				controller.setReg(a, Register.REG_VM,
						VMStatus.VMSTATUS_RUNNING.ordinal());
				int status = controller.getReg(a, Register.REG_VM);
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
