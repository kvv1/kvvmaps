package kvv.stdutils.cmdline;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class CmdLine {
	private Map<String, CmdLineParam<?>> params = new LinkedHashMap<String, CmdLineParam<?>>();
	public String[] args;

	public void add(CmdLineParam<?> param) {
		params.put(param.name, param);
	}

	public void parse(String[] args) {
		ArrayList<String> arr = new ArrayList<String>();

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			CmdLineParam<?> param = params.get(arg);
			if (param != null) {
				if (param.needsArg)
					param.set(args[++i]);
				else
					param.set();
			} else {
				if(arg.startsWith("-"))
					throw new IllegalArgumentException("unknown param '" + arg + "'");
				arr.add(arg);
			}
		}

		this.args = arr.toArray(new String[0]);
	}

	public void printHelp(PrintStream ps) {
		for (CmdLineParam<?> param : params.values()) {
			ps.print(param.name);
			if (param.needsArg)
				ps.print(" <value>");
			ps.print(" ");
		}
		ps.println();
	}

	public void print(PrintStream ps) {
		for (CmdLineParam<?> param : params.values()) {
			ps.println(param.name + "=" + param.value);
		}
	}
}
