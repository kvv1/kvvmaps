package kvv.stdutils.cmdline;

public class StringParam extends CmdLineParam<String>{
	public StringParam(CmdLine cmdLine, String name, String defaultValue) {
		super(cmdLine, name, defaultValue, true);
	}

	@Override
	public void set(String svalue) {
		value = svalue;
	}

	@Override
	public void set() {
	}

}
