package kvv.controllers.utils.cmdline;

public class FloatParam extends CmdLineParam<Float>{
	public FloatParam(CmdLine cmdLine, String name, float defaultValue) {
		super(cmdLine, name, defaultValue, true);
	}

	@Override
	public void set(String svalue) {
		value = Float.valueOf(svalue);
	}

	@Override
	public void set() {
	}

}
