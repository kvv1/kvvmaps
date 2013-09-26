package kvv.evlang.impl;

import java.io.PrintStream;

import kvv.evlang.ParseException;

public class CodeRef {
		public int off;
		public int len;
		private final Context context;

//		public CodeRef() {
//		}

		public CodeRef(Context context, Code code) {
			this.context = context;
			this.off = context.codeArr.size();
			context.codeArr.addAll(code.code);
			this.len = context.codeArr.size() - off;
		}

		public int check(int expected, String msg) throws ParseException {
			int maxStack = 0;
			int stack = 0;
			int locals = 0;
			for (int i = off; i < off + len; i++) {
				int bc1 = context.codeArr.get(i) & 0xC0;
				if (bc1 == BC.GETREGSHORT) {
					EG.dumpStream.print("GETREGSHORT ");
					stack++;
				} else if (bc1 == BC.SETREGSHORT) {
					EG.dumpStream.print("SETREGSHORT ");
					stack--;
				} else if (bc1 == BC.LITSHORT) {
					EG.dumpStream.print("LITSHORT ");
					stack += 1;
				} else {
					bc1 = context.codeArr.get(i);

					BC bc = BC.values()[bc1];

					EG.dumpStream.print(bc.name() + " ");

					if (bc == BC.CALLF || bc == BC.CALLP) {
						PrintStream ps = EG.dumpStream;
						EG.dumpStream = EG.nullStream;
						Func f = context.funcDefList.get(context.codeArr.get(i + 1));
						maxStack = Math.max(maxStack,
								stack + 2 + f.getMaxStack());
						stack -= f.args;
						if (f.retSize)
							stack++;
						i += bc.args;
						EG.dumpStream = ps;
					} else if (bc == BC.ENTER) {
						i++;
						locals = context.codeArr.get(i);
						stack += locals;
					} else if (bc == BC.RET || bc == BC.RETI || bc == BC.RET_N
							|| bc == BC.RETI_N) {
						stack -= locals;
						break;
					} else {
						stack += bc.stackBalance;
						i += bc.args;
					}
				}
				if (stack < 0)
					throw new ParseException(msg + " stack underflow");

				maxStack = Math.max(maxStack, stack);
			}
			if (stack != expected)
				throw new ParseException(msg + " stack error");

			return maxStack;
		}
	}