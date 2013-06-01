package kvv.controllers.register;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("serial")
public class AllRegs implements Serializable {
	public ArrayList<RegisterUI> ui;
	public HashMap<Integer, Integer> values;

	public AllRegs() {
	}

	public AllRegs(ArrayList<RegisterUI> ui, HashMap<Integer, Integer> map) {
		this.ui = ui;
		this.values = map;
	}
}
