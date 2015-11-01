package kvv.controller.register;

import java.io.Serializable;
import java.util.HashMap;


@SuppressWarnings("serial")
public class AllRegs implements Serializable {
	public int addr;
	public HashMap<Integer, Integer> values;

	public AllRegs() {
	}

	public AllRegs(int addr, HashMap<Integer, Integer> map) {
		this.addr = addr;
		this.values = map;
	}
}
