package kvv.stdutils.cmdline;


public class BooleanParam extends CmdLineParam<Boolean> {
	public BooleanParam(CmdLine cmdLine, String name) {
		super(cmdLine, name, false, false);
	}

	@Override
	public void set(String svalue) {
//		if (svalue.equalsIgnoreCase("true"))
//			value = true;
//		else if (svalue.equalsIgnoreCase("false"))
//			value = false;
//		else
//			throw new NumberFormatException(svalue);
	}

	@Override
	public void set() {
		value = true;
	}

}
