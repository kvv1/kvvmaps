package kvv.controllers.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ControllerDef implements Serializable {

	public boolean hasRules;
	public int[] allRegs;

	public RegisterDef[] registers;

	public static class RegisterDef implements Serializable {
		public Integer n;
		public String name;
		public int[] validRanges;
	}

	public RegisterDef getReg(int n) {
		if (registers == null)
			return null;
		for (RegisterDef registerDef : registers)
			if (registerDef.n == n)
				return registerDef;
		return null;
	}
}
