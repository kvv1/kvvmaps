package kvv.evlang.rt;

public enum BC_SHORT {
	GETREG_SHORT ( 0x40),
	SETREG_SHORT ( 0x50),
	GETLOCAL_SHORT ( 0x60),
	SETLOCAL_SHORT ( 0x70),
	GETFIELD_SHORT ( 0x80),
	SETFIELD_SHORT ( 0x90),
	CALL_SHORT ( 0xA0),
	RET_SHORT ( 0xB0),
	RETI_SHORT ( 0xC0),
	LIT_SHORT ( 0xD0),
	ENTER_SHORT ( 0xE0),
	NEW_SHORT ( 0xF0);
	
	public int n;
	
	BC_SHORT(int n) {
		this.n = n;
	}

	public static BC_SHORT fromN(int n) {
		BC_SHORT[] vals = values();
		for(BC_SHORT bc : vals)
			if(bc.n == n)
				return bc;
		return null;
	}
}
