package kvv.controllers.server;

import java.util.HashMap;
import java.util.Map;

import kvv.controllers.server.utils.Constants;
import kvv.controllers.server.utils.Utils;
import kvv.controllers.shared.Register;

public class Registers {

	private static Map<String, Register> map = new HashMap<String, Register>();
	static {
		try {
			Register[] registers = Utils.jsonRead(Constants.registersFile,
					Register[].class);
			for (Register register : registers)
				if (register != null)
					map.put(register.name, register);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized static Register getRegister(String name)
			throws Exception {
		Register reg = map.get(name);
		if (reg == null)
			throw new Exception("Регистр " + name + " не определен");
		return reg;
	}

}
