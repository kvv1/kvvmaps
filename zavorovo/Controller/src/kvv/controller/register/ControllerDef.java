package kvv.controller.register;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ControllerDef implements Serializable {

	public Integer regEEPROM0;
	public Integer regRAM0;
	public Integer lastReg;
	public Integer lastEEReg;

	public Integer regVmOnOff;
	public Integer regVmState;

	public int[] allRegs;

	public RegisterDef[] registers;

	public static class RegisterDef implements Serializable {
		public Integer n;
		public String name;
		public Integer mul;
		public int[] validRanges;
		public int[] bitMapping;
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
