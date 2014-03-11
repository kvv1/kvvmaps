package kvv.evlang.rt;

public enum BC {
	LIT(1, 2), //
	SETREG(-1, 1), GETREG(1, 1), //
	SETTIMER_S(-1, 1), SETTIMER_MS(-1, 1), STOPTIMER(0, 1), //
	QBRANCH(-1, 1), BRANCH(0, 1), //
	ADD(-1, 0), SUB(-1, 0), MUL(-1, 0), DIV(-1, 0), NEGATE(0, 0), //
	OR(-1, 0), AND(-1, 0), NOT(0, 0), //
	LT(-1, 0), LE(-1, 0), GT(-1, 0), GE(-1, 0), EQ(-1, 0), NEQ(-1, 0), //
	DROP(-1, 0), //
	INC(0, 1), DEC(0, 1), //
	CALL(0, 1), //
	MULDIV(-2, 0), //
	PRINT(-1, 0), 
	SETEXTREG(-1, 2), GETEXTREG(1, 2),
	THROW(-1, 0), NEW(1, 1), VCALL(0, 1), TRAP(0,0), 
	STOPTRIGGER(0,0), SETTRIGGER(0,0), 
	NEWOBJARR(0,0), NEWINTARR(0,0), SETARRAY(0,0), GETARRAY(0,0), ARRAYLENGTH(0,0);

	public int stackBalance;
	public int args;

	public static final int GETREG_SHORT = 0x40;
	public static final int SETREG_SHORT = 0x50;
	public static final int GETLOCAL_SHORT = 0x60;
	public static final int SETLOCAL_SHORT = 0x70;
	public static final int GETFIELD_SHORT = 0x80;
	public static final int SETFIELD_SHORT = 0x90;
	public static final int CALL_SHORT = 0xA0;
	public static final int RET_SHORT = 0xB0;
	public static final int RETI_SHORT = 0xC0;
	public static final int LIT_SHORT = 0xD0;
	public static final int ENTER_SHORT = 0xE0;
	public static final int NEW_SHORT = 0xF0;

//	public static final int OR_BRANCH_SHORT = 0xE0;
//	public static final int AND_BRANCH_SHORT = 0xF0;

	BC(int stackBalance, int args) {
		this.stackBalance = stackBalance;
		this.args = args;
	}
}