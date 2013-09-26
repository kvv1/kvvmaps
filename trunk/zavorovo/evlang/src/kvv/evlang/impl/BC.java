package kvv.evlang.impl;

public enum BC {
	RET(0, 0), RET_N(0, 1), RETI(-1, 0), RETI_N(-1, 1), LIT(1, 2), //
	SETREG(-1, 1), GETREG(1, 1), //
	SETTIMER_S(-1, 1), SETTIMER_MS(-1, 1), STOPTIMER(0, 1), //
	QBRANCH(-1, 1), BRANCH(0, 1), //
	ADD(-1, 0), SUB(-1, 0), MUL(-1, 0), DIV(-1, 0), NEGATE(0, 0), //
	OR(-1, 0), AND(-1, 0), NOT(0, 0), //
	LT(-1, 0), LE(-1, 0), GT(-1, 0), GE(-1, 0), EQ(-1, 0), NEQ(-1, 0), //
	PRINT(-1, 0), //
	INC(0, 1), DEC(0, 1), //
	CALLP(0, 1), CALLF(0, 1), //
	MULDIV(-2, 0), //
	GETLOCAL(1, 1), SETLOCAL(-1, 1), ENTER(0,1);

	public int stackBalance;
	public int args;

	public static final int GETREGSHORT = 0x80;
	public static final int SETREGSHORT = 0xC0;
	public static final int LITSHORT = 0x40;

	BC(int stackBalance, int args) {
		this.stackBalance = stackBalance;
		this.args = args;
	}
}