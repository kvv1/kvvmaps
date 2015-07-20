package kvv.stdutils.cmdline;

public abstract class CmdLineParam<T> {
	public final String name;
	public T value;
	public final boolean needsArg;
	
	public CmdLineParam(CmdLine cmdLine, String name, T defaultValue, boolean needsArg) {
		this.name = name;
		this.value = defaultValue;
		this.needsArg = needsArg;
		cmdLine.add(this);
	}
	
	public abstract void set(String value);
	public abstract void set();
}
