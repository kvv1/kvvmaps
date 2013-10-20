package kvv.evlang.rt;

import java.util.List;

public abstract class Interpreter {
	public abstract void setExtReg(int addr, int reg, int value);

	public abstract int getExtReg(int addr, int reg);

	static class Stack {
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

	public void interpret(RTContext context, int off) {
		if (!stack.isEmpty())
			throw new IllegalStateException();
		_interpret(context, off);
		if (!stack.isEmpty())
			throw new IllegalStateException();
	}

	public int eval(RTContext context, int off) {
		if (!stack.isEmpty())
			throw new IllegalStateException();
		_interpret(context, off);
		int res = stack.pop();
		if (!stack.isEmpty())
			throw new IllegalStateException();
		return res;
	}

	private void _interpret(RTContext context, int ip) {
		// System.out.println(code.size());

		List<Byte> code = context.codeArr;

		int fp = stack.getSP();

		while (true) {
			int right;
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
				stack.push(ip);
				ip = addr;
				stack.push(fp);
				fp = stack.getSP();
				break;
			case RET:
				stack.sp = fp;
				if (stack.isEmpty())
					return;
				fp = stack.pop();
				ip = stack.pop();
				break;
			case RET_N:
				stack.sp = fp;
				if (stack.isEmpty())
					return;
				fp = stack.pop();
				int ip1 = stack.pop();
				int n = code.get(ip++);
				while (n-- > 0)
					stack.pop();
				ip = ip1;
				break;
			case RETI:
				int res = stack.pop();
				stack.sp = fp;
				if (stack.isEmpty()) {
					stack.push(res);
					return;
				}
				fp = stack.pop();
				ip = stack.pop();
				stack.push(res);
				break;
			case RETI_N:
				res = stack.pop();
				stack.sp = fp;
				fp = stack.pop();
				ip1 = stack.pop();
				n = code.get(ip++);
				while (n-- > 0)
					stack.pop();
				ip = ip1;
				stack.push(res);
				break;
			case GETLOCAL:
				n = code.get(ip++);
				if (n >= 0)
					stack.push(stack.getAt(fp + 2 + n));
				else
					stack.push(stack.getAt(fp + n));
				break;
			case SETLOCAL:
				n = code.get(ip++);
				if (n >= 0)
					stack.setAt(fp + 2 + n, stack.pop());
				else
					stack.setAt(fp + n, stack.pop());
				break;
			case ENTER:
				n = code.get(ip++);
				while (n-- > 0)
					stack.push(0);
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
				stack.push(stack.pop() / right);
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
				stack.push(n1 * n2 / n3);
				break;
			}
			default:
				throw new RuntimeException("unknown bytecode " + c);
			}
		}

	}
}
