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
		private int[] data = new int[100];
		private int sp = data.length;

		public boolean isEmpty() {
			return sp == data.length;
		}

		public int pop() {
			return data[sp++];
		}

		public void push(int n) {
			data[--sp] = n;
		}

		public int getSP() {
			return sp;
		}

		public int getAt(int off) {
			return data[off];
		}

		public void setAt(int off, int n) {
			data[off] = n;
		}
	}

	private Stack stack = new Stack();
	private int ip;
	private int fp;

	public void interpret(int off) throws UncaughtExceptionException {
		if (!stack.isEmpty())
			throw new IllegalStateException();
		if (ip != 0)
			throw new IllegalStateException();
		if (fp != 0)
			throw new IllegalStateException();
		_interpret(off);
		if (!stack.isEmpty())
			throw new IllegalStateException();
	}

	public int eval(int off) throws UncaughtExceptionException {
		if (!stack.isEmpty())
			throw new IllegalStateException();
		if (ip != 0)
			throw new IllegalStateException();
		if (fp != 0)
			throw new IllegalStateException();
		_interpret(off);
		int res = stack.pop();
		if (!stack.isEmpty())
			throw new IllegalStateException();
		return res;
	}

	private void call(int addr) {
		stack.push(ip);
		stack.push(fp);
		fp = stack.getSP();
		ip = addr;
	}

	private void ret() {
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

	private void _interpret(int _ip)
			throws UncaughtExceptionException {
		List<Byte> code = context.codeArr;

		call(_ip);

		while (true) {
			int right;
			int left;
			int off;
			int timer;

			byte c = code.get(ip++);
			if ((c & 0xC0) == BC.GETREGSHORT) {
				int reg = c & 0x3F;
				stack.push(context.regs[reg]);
				continue;
			} else if ((c & 0xC0) == BC.SETREGSHORT) {
				int reg = c & 0x3F;
				context.regs[reg] = stack.pop();
				continue;
			} else if ((c & 0xC0) == BC.LITSHORT) {
				int val = (c & 0x3F) << 26 >> 26;
				stack.push(val);
				continue;
			}

			BC bc = BC.values()[c];
			switch (bc) {
			case CALL:
				int addr = context.funcs[code.get(ip++)].code;
				call(addr);
				break;
			case RET:
				ret();
				if (ip == 0)
					return;
				break;
			case RET_N:
				int n = code.get(ip++);
				ret();
				while (n-- > 0)
					stack.pop();
				if (ip == 0)
					return;
				break;
			case RETI:
				int res = stack.pop();
				ret();
				stack.push(res);
				if (ip == 0)
					return;
				break;
			case RETI_N:
				n = code.get(ip++);
				res = stack.pop();
				ret();
				while (n-- > 0)
					stack.pop();
				stack.push(res);
				if (ip == 0)
					return;
				break;

			case THROW:
				res = stack.pop();
				throwException(res);
				break;

			case GETLOCAL:
				n = code.get(ip++);
				stack.push(stack.getAt(fp + 2 + n));
				break;
			case SETLOCAL:
				n = code.get(ip++);
				stack.setAt(fp + 2 + n, stack.pop());
				break;
			case ENTER:
				int link = stack.pop();
				int ret = stack.pop();
				n = code.get(ip++);
				while (n-- > 0)
					stack.push(0);
				stack.push(ret);
				stack.push(link);
				fp = stack.sp;
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
				stack.push(context.regs[reg]);
				break;
			case SETREG:
				int reg1 = code.get(ip++) & 0xFF;
				context.regs[reg1] = stack.pop();
				break;
			case GETEXTREG:
				addr = code.get(ip++) & 0xFF;
				reg = code.get(ip++) & 0xFF;
				stack.push(getExtReg(addr, reg));
				break;
			case SETEXTREG:
				addr = code.get(ip++) & 0xFF;
				reg = code.get(ip++) & 0xFF;
				setExtReg(addr, reg, stack.pop());
				break;
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
				timer = code.get(ip++);
				context.timers[timer].cnt = stack.pop();
				break;
			case SETTIMER_S:
				timer = code.get(ip++);
				context.timers[timer].cnt = stack.pop() * 1000;
				break;
			case STOPTIMER:
				timer = code.get(ip++);
				context.timers[timer].cnt = 0;
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
			default:
				throw new RuntimeException("unknown bytecode " + c);
			}
		}

	}
}
