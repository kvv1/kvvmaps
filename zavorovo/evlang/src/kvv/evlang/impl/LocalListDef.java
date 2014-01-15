package kvv.evlang.impl;

import java.util.ArrayList;
import java.util.List;

public class LocalListDef {

	private List<String> locals = new ArrayList<String>();
	private int argCnt;

	private int max;

	public int getMax() {
		return max;
	}

	public void add(String name) {
		System.out.println("LOCAL " + name + " : " + locals.size());
		locals.add(name);
		max = Math.max(max, locals.size());
	}

	public void endOfArgs() {
		argCnt = locals.size();
	}

//	public Integer getIdx(String name) {
//		Integer idx = get(name);
//		if(idx == null)
//			return null;
//		return argCnt - idx - 1;
//	}
	
	public int getArgCnt() {
		return argCnt;
	}

	public Integer get(String name) {
		for (int i = 0; i < locals.size(); i++)
			if (locals.get(i).equals(name))
				return i;
		return null;
	}

	public int getSize() {
		return locals.size();
	}

	public void setSize(int size) {
		locals = new ArrayList<String>(locals.subList(0, size));
	}
}