package kvv.evlang.impl;

import java.io.PrintStream;

import kvv.evlang.ParseException;
import kvv.evlang.rt.BC;

public class CodeRef {
	public short off;
	public short len;
	private final Context context;

	// public CodeRef() {
	// }

	public CodeRef(Code code) {
		this.context = code.context;
		this.off = context.codeArr.size();
		context.codeArr.addAll(code);
		this.len = code.size();
	}
/*
	public int check(String retType, String msg) throws ParseException {
		int maxStack = 0;
		int stack = 0;
		int locals = 0;
		for (int i = off; i < off + len; i++) {
			int bc1 = context.codeArr.code.get(i) & 0xC0;
			if (bc1 == BC.GETREGSHORT) {
				context.dumpStream.print("GETREGSHORT ");
				stack++;
			} else if (bc1 == BC.SETREGSHORT) {
				context.dumpStream.print("SETREGSHORT ");
				stack--;
			} else if (bc1 == BC.LITSHORT) {
				context.dumpStream.print("LITSHORT ");
				stack += 1;
			} else {
				bc1 = context.codeArr.code.get(i);

				BC bc = BC.values()[bc1];

				context.dumpStream.print(bc.name() + " ");

				if (bc == BC.CALL) {
					PrintStream ps = context.dumpStream;
					context.dumpStream = EG.nullStream;
					Func f = context.funcDefList.get(context.codeArr.code
							.get(i + 1));
					maxStack = Math.max(maxStack, stack + 2 + f.getMaxStack());
					stack -= f.locals.getArgCnt();
					stack += f.retSize;
					i += bc.args;
					context.dumpStream = ps;
				} else if (bc == BC.ENTER) {
					i++;
					locals = context.codeArr.code.get(i);
					stack += locals;
				} else if (bc == BC.RET || bc == BC.RETI || bc == BC.RET_N
						|| bc == BC.RETI_N) {
					i += bc.args;
					// stack -= locals;
					if (stack - locals != expected)
						throw new ParseException(msg + " stack error");

					stack += bc.stackBalance;
					// break;
				} else {
					stack += bc.stackBalance;
					i += bc.args;
				}
			}
			if (stack < 0)
				throw new ParseException(msg + " stack underflow");

			maxStack = Math.max(maxStack, stack);
		}
		// if (stack != expected)
		// throw new ParseException(msg + " stack error");

		return maxStack;
	}
*/	
}