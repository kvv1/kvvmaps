package kvv.evlang.rt;

import java.util.List;

public abstract class Interpreter {

	private final RTContext context;

	public Interpreter(RTContext cont) {
		this.context = cont;
	}

	public abstract void setExtReg(int addr, int reg, int value);

	public abstract int getExtReg(int addr, int reg);

	static private class Stack {
		private short[] data = new short[100];
		private short sp = (short) data.length;

		public boolean isEmpty() {
			return sp == data.length;
		}

		public short pop() {
			return data[sp++];
		}

		public void push(int n) {
			data[--sp] = (short) n;
		}

		public short getSP() {
			return sp;
		}

		public short getAt(int off) {
			return data[off];
		}

		public void setAt(int off, short n) {
			data[off] = n;
		}
	}

	private Stack stack = new Stack();
	private short ip;
	private short fp;

	public void interpret(short off, Short... params)
			throws UncaughtExceptionException {
		if (!stack.isEmpty() || ip != 0 || fp != 0)
			throw new IllegalStateException();
		if (params != null)
			for (Short param : params)
				stack.push(param);
		_interpret(off);
		if (!stack.isEmpty() || ip != 0 || fp != 0)
			throw new IllegalStateException();
	}

	public short eval(short off, Short... params) throws UncaughtExceptionException {
		if (!stack.isEmpty() || ip != 0 || fp != 0)
			throw new IllegalStateException();
		if (params != null)
			for (Short param : params)
				stack.push(param);
		_interpret(off);
		short res = stack.pop();
		if (!stack.isEmpty() || ip != 0 || fp != 0)
			throw new IllegalStateException();
		return res;
	}

	private void call(short addr) {
		stack.push(ip);
		stack.push(fp);
		fp = stack.getSP();
		ip = addr;
	}

	private void ret() {
		if (stack.sp > fp)
			throw new IllegalStateException();
		stack.sp = fp;
		fp = stack.pop();
		ip = stack.pop();
	}

	private void throwException(int e) throws UncaughtExceptionException {
		for (;;) {
			stack.sp = fp;
			TryCatchBlock tcb = context.findTryCatchBlock(ip);
			if (tcb != null) {
				stack.push(e);
				ip = tcb.handler;
				break;
			} else {
				ret();
				if (ip == 0)
					throw new UncaughtExceptionException();
			}
		}
	}

	private void _interpret(short _ip) throws UncaughtExceptionException {
		List<Byte> code = context.codeArr;

		call(_ip);

		while (true) {
			int right;
			int left;
			int off;

			byte c = code.get(ip++);

			if ((c & 0xC0) != 0) {
				short param = (short) (c & 0x0F);
				switch (c & 0xF0) {
				case BC.LIT_SHORT:
					stack.push(context.constPool[param]);
					break;
				case BC.GETREG_SHORT:
					getreg(context.regPool[param]);
					break;
				case BC.SETREG_SHORT:
					setreg(context.regPool[param]);
					break;
				case BC.GETLOCAL_SHORT:
					stack.push(stack.getAt(fp + 2 + param));
					break;
				case BC.SETLOCAL_SHORT:
					stack.setAt(fp + 2 + param, stack.pop());
					break;
				case BC.GETFIELD_SHORT:
					short a = stack.pop();
					if (a == 0)
						throwException(Exc.NULLPOINTER_EXCEPTION.ordinal());
					else
						stack.push(context.heap.get(a, param));
					break;
				case BC.SETFIELD_SHORT:
					short n = stack.pop();
					a = stack.pop();
					if (a == 0)
						throwException(Exc.NULLPOINTER_EXCEPTION.ordinal());
					else
						context.heap.set(a, param, n);
					break;
				case BC.CALL_SHORT:
					short addr = context.funcs[param];
					call(addr);
					break;
				case BC.RETI_SHORT:
					short res = stack.pop();
					ret();
					while (param-- > 0)
						stack.pop();
					stack.push(res);
					if (ip == 0)
						return;
					break;
				case BC.RET_SHORT:
					ret();
					while (param-- > 0)
						stack.pop();
					if (ip == 0)
						return;
					break;
				case BC.ENTER_SHORT:
					short link = stack.pop();
					short ret = stack.pop();
					while (param-- > 0)
						stack.push(0);
					stack.push(ret);
					stack.push(link);
					fp = stack.sp;
					break;
				default:
					throw new RuntimeException("unknown short bytecode "
							+ (c & 0xF0));
				}
				continue;
			}

			BC bc = BC.values()[c];
			switch (bc) {
			case CALL: {
				short addr = context.funcs[code.get(ip++)];
				call(addr);
				break;
			}

			case VCALL: {
				byte arg = code.get(ip++);
				int argCnt = (arg & 0xFF) >>> 4;
				int n = arg & 0x0f;
				int obj = stack.getAt(stack.sp + argCnt - 1);
				if (obj == 0) {
					throwException(Exc.NULLPOINTER_EXCEPTION.ordinal());
				} else {
					int type = context.heap.getTypeIdx(obj);
					short addr = context.types[type].vtable[n];
					call(addr);
				}
				break;
			}

			case THROW:
				short res = stack.pop();
				throwException(res);
				break;

			case ENTER:
				int link = stack.pop();
				int ret = stack.pop();
				short n = code.get(ip++);
				while (n-- > 0)
					stack.push(0);
				stack.push(ret);
				stack.push(link);
				fp = stack.sp;
				break;
			case NEW:
				n = code.get(ip++);
				int sz = context.types[n].sz;
				short a = context.heap.alloc(n, false, false);
				if (a == 0)
					throwException(Exc.OUTOFMEMORY_EXCEPTION.ordinal());
				else {
					while (sz-- > 0)
						context.heap.set(a, sz, stack.pop());
					stack.push(a);
				}
				break;
			case ADD:
				stack.push(stack.pop() + stack.pop());
				break;
			case SUB:
				stack.push(-(stack.pop() - stack.pop()));
				break;
			case MUL:
				stack.push(stack.pop() * stack.pop());
				break;
			case DIV:
				right = stack.pop();
				left = stack.pop();
				if (right == 0)
					throwException(Exc.ARITHMETIC_EXCEPTION.ordinal());
				else
					stack.push(left / right);
				break;
			case AND: {
				int n1 = stack.pop();
				int n2 = stack.pop();
				stack.push((n1 != 0 && n2 != 0) ? 1 : 0);
				break;
			}
			case OR: {
				int n1 = stack.pop();
				int n2 = stack.pop();
				stack.push((n1 != 0 || n2 != 0) ? 1 : 0);
				break;
			}
			case EQ:
				stack.push((stack.pop() == stack.pop()) ? 1 : 0);
				break;
			case NEQ:
				stack.push((stack.pop() != stack.pop()) ? 1 : 0);
				break;
			case GT:
				right = stack.pop();
				stack.push((stack.pop() > right) ? 1 : 0);
				break;
			case GE:
				right = stack.pop();
				stack.push((stack.pop() >= right) ? 1 : 0);
				break;
			case LT:
				right = stack.pop();
				stack.push((stack.pop() < right) ? 1 : 0);
				break;
			case LE:
				right = stack.pop();
				stack.push((stack.pop() <= right) ? 1 : 0);
				break;
			case LIT:
				byte hi = code.get(ip++);
				byte lo = code.get(ip++);
				stack.push((hi << 8) + (lo & 0xFF));
				break;
			case NOT:
				stack.push(stack.pop() != 0 ? 0 : 1);
				break;
			case NEGATE:
				stack.push(-stack.pop());
				break;
			case GETREG:
				int reg = code.get(ip++) & 0xFF;
				getreg(reg);
				break;
			case SETREG:
				int reg1 = code.get(ip++) & 0xFF;
				setreg(reg1);
				break;
			case GETEXTREG: {
				short addr = (short) (code.get(ip++) & 0xFF);
				reg = code.get(ip++) & 0xFF;
				stack.push(getExtReg(addr, reg));
				break;
			}
			case SETEXTREG: {
				short addr = (short) (code.get(ip++) & 0xFF);
				reg = code.get(ip++) & 0xFF;
				setExtReg(addr, reg, stack.pop());
				break;
			}
			case BRANCH:
				off = code.get(ip++);
				ip += off;
				break;
			case QBRANCH:
				off = code.get(ip++);
				if (stack.pop() == 0)
					ip += off;
				break;
			case DROP:
				stack.pop();
				break;
			case PRINT:
				System.out.print(stack.pop() + " ");
				break;
			case SETTIMER_MS:
				short ms = stack.pop();
				short obj = stack.pop();
				if (obj == 0)
					throwException(Exc.NULLPOINTER_EXCEPTION.ordinal());
				else
					context.setTimer(obj, ms);
				break;
			// case SETTIMER_S:
			// timer = code.get(ip++);
			// context.timers[timer].cnt = stack.pop() * 1000;
			// break;
			case STOPTIMER:
				obj = stack.pop();
				if (obj == 0)
					throwException(Exc.NULLPOINTER_EXCEPTION.ordinal());
				else
					context.stopTimer(obj);
				break;
			case SETTRIGGER:
				short initVal = stack.pop();
				obj = stack.pop();
				if (obj == 0)
					throwException(Exc.NULLPOINTER_EXCEPTION.ordinal());
				else
					context.setTrigger(obj, initVal);
				break;
			case STOPTRIGGER:
				obj = stack.pop();
				if (obj == 0)
					throwException(Exc.NULLPOINTER_EXCEPTION.ordinal());
				else
					context.stopTrigger(obj);
				break;
			case INC:
				reg = code.get(ip++) & 0xFF;
				context.regs[reg]++;
				break;
			case DEC:
				reg = code.get(ip++) & 0xFF;
				context.regs[reg]--;
				break;
			case MULDIV: {
				int n3 = stack.pop();
				int n2 = stack.pop();
				int n1 = stack.pop();
				if (n3 == 0)
					throwException(Exc.ARITHMETIC_EXCEPTION.ordinal());
				else
					stack.push(n1 * n2 / n3);
				break;
			}
			case TRAP:
				break;
			default:
				throw new RuntimeException("unknown bytecode " + c);
			}
		}

	}

	private void setreg(int reg) {
		context.regs[reg] = stack.pop();
	}

	private void getreg(int reg) {
		stack.push(context.regs[reg]);
	}
}
