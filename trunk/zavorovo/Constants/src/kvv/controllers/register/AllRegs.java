package kvv.controllers.register;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("serial")
public class AllRegs implements Serializable {
	public int addr;
	public ArrayList<RegisterUI> ui;
	public HashMap<Integer, Integer> values;

	public AllRegs() {
	}

	public AllRegs(int addr, ArrayList<RegisterUI> ui, HashMap<Integer, Integer> map) {
		this.addr = addr;
		this.ui = ui;
		this.values = map;
	}
}
