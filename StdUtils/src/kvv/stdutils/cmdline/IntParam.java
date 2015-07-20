package kvv.stdutils.cmdline;

public class IntParam extends CmdLineParam<Integer>{
	public IntParam(CmdLine cmdLine, String name, Integer defaultValue) {
		super(cmdLine, name, defaultValue, true);
	}

	@Override
	public void set(String svalue) {
		value = Integer.valueOf(svalue);
	}

	@Override
	public void set() {
	}

}
