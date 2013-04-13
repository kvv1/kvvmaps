package kvv.evlang.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Code {
	public List<Byte> code = new ArrayList<Byte>();

	static Map<BC, Integer> histo = new HashMap<BC, Integer>();

	static void printHisto() {
		for (BC c : histo.keySet()) {
			System.out.println(c.name() + " " + histo.get(c));
		}
	}

	public void add(BC c) {
		code.add((byte) c.ordinal());
		Integer n = histo.get(c);
		histo.put(c, n == null ? 1 : n + 1);
	}

	public void add(int c) {
		code.add((byte) c);
	}

	public void addAll(Code c) {
		code.addAll(c.code);
	}

	public int size() {
		return code.size();
	}

	public void compileLit(short s) {
		if (s >= -32 && s < 32) {
			add(BC.LITSHORT + (s & 0x3F));
		} else {
			add(BC.LIT);
			add(s >>> 8);
			add(s & 0xFF);
		}
	}

	public void compileGetreg(int reg) {
		if (reg < 64) {
			add(BC.GETREGSHORT + reg);
		} else {
			add(BC.GETREG);
			add(reg);
		}
	}

	public void compileSetreg(int reg) {
		if (reg < 64) {
			add(BC.SETREGSHORT + reg);
		} else {
			add(BC.SETREG);
			add(reg);
		}
	}

	public void compileGetLocal(int loc) {
		add(BC.GETLOCAL);
		add(loc);
	}

	public void compileSetLocal(int loc) {
		add(BC.SETLOCAL);
		add(loc);
	}
}