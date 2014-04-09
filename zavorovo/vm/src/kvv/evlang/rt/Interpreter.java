package kvv.evlang.rt;

import java.io.IOException;
import java.util.List;

public abstract class Interpreter {

	private final RTContext context;

	public Interpreter(RTContext cont) {
		this.context = cont;
	}

	public abstract void setExtReg(int addr, int reg, int value)
			throws IOException;

	public abstract int getExtReg(int addr, int reg) throws IOException;

	private short ip;
	private short fp;

	public void interpret(short off, Short... params)
			throws UncaughtExceptionException {
		if (!context.stack.isEmpty() || ip != 0 || fp != 0)
			throw new IllegalStateException();
		if (params != null)
			for (Short param : params)
				context.stack.push(param);
		_interpret(off);
		if (!context.stack.isEmpty() || ip != 0 || fp != 0)
			throw new IllegalStateException();
	}

	public short eval(short off, Short... params)
			throws UncaughtExceptionException {
		if (!context.stack.isEmpty() || ip != 0 || fp != 0)
			throw new IllegalStateException();
		if (params != null)
			for (Short param : params)
				context.stack.push(param);
		_interpret(off);
		short res = context.stack.pop();
		if (!context.stack.isEmpty() || ip != 0 || fp != 0)
			throw new IllegalStateException();
		return res;
	}

	private void call(short addr) {
		context.stack.push(ip);
		context.stack.push(fp);
		fp = context.stack.getSP();
		ip = addr;
	}

	private void ret() {
		if (context.stack.getSP() > fp)
			throw new IllegalStateException();
		context.stack.setSP(fp);
		fp = context.stack.pop();
		ip = context.stack.pop();
	}

	private void throwException(int e) throws UncaughtExceptionException {
		for (;;) {
			context.stack.setSP(fp);
			TryCatchBlock tcb = context.findTryCatchBlock(ip);
			if (tcb != null) {
				context.stack.push(e);
				ip = tcb.handler;
				break;
			} else {
				ret();
				if (ip == 0) {
					System.out.println("UncaughtException " + Exc.values()[e]);
					throw new UncaughtExceptionException();
				}
			}
		}
	}

	private void _interpret(short _ip) throws UncaughtExceptionException {
		List<Byte> code = context.code;

		call(_ip);

		while (true) {
			int right;
			int left;
			int off;

			byte c = code.get(ip++);

			if ((c & 0xC0) != 0) {
				short param = (short) (c & 0x0F);
				switch (c & 0xF0) {
				case BC.GETREG_SHORT:
					getreg(context.regPool[param]);
					break;
				case BC.SETREG_SHORT:
					setreg(context.regPool[param]);
					break;
				case BC.GETLOCAL_SHORT:
					context.stack.push(context.stack.getAt(fp, 2 + param));
					break;
				case BC.SETLOCAL_SHORT:
					context.stack.setAt(fp, 2 + param, context.stack.pop());
					break;
				case BC.GETFIELD_SHORT:
					short a = context.stack.pop();
					if (a == 0) {
						throwException(Exc.NULLPOINTER_EXCEPTION.ordinal());
						break;
					}
					context.stack.push(context.heap.get(a, param));
					break;
				case BC.SETFIELD_SHORT:
					short n = context.stack.pop();
					a = context.stack.pop();
					if (a == 0) {
						throwException(Exc.NULLPOINTER_EXCEPTION.ordinal());
						break;
					}
					context.heap.set(a, param, n);
					break;
				case BC.CALL_SHORT:
					short addr = context.funcs[param];
					call(addr);
					break;
				case BC.RETI_SHORT:
					short res = context.stack.pop();
					ret();
					while (param-- > 0)
						context.stack.pop();
					context.stack.push(res);
					if (ip == 0)
						return;
					break;
				case BC.RET_SHORT:
					ret();
					while (param-- > 0)
						context.stack.pop();
					if (ip == 0)
						return;
					break;
				case BC.LIT_SHORT:
					context.stack.push(context.constPool[param]);
					break;
				case BC.ENTER_SHORT:
					short link = context.stack.pop();
					short ret = context.stack.pop();
					while (param-- > 0)
						context.stack.push(0);
					context.stack.push(ret);
					context.stack.push(link);
					fp = context.stack.getSP();
					break;
				case BC.NEW_SHORT:
					_new(param);
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
				int obj = context.stack.pick(argCnt - 1);
				if (obj == 0) {
					throwException(Exc.NULLPOINTER_EXCEPTION.ordinal());
					break;
				}
				int type = context.heap.getTypeIdx(obj);
				short addr = context.types[type].vtable[n];
				call(addr);
				break;
			}

			case THROW:
				short res = context.stack.pop();
				throwException(res);
				break;

			case NEW:
				short n = code.get(ip++);
				_new(n);
				break;
			case NEWINTARR:
				short sz = context.stack.pop();
				_newArr(sz, false);
				break;
			case NEWOBJARR:
				sz = context.stack.pop();
				_newArr(sz, true);
				break;
			case SETARRAY:
				int val = context.stack.pop();
				int idx = context.stack.pop();
				int a = context.stack.pop();
				if (a == 0) {
					throwException(Exc.NULLPOINTER_EXCEPTION.ordinal());
					break;
				}
				if (idx < 0 || idx >= context.heap.getArraySize(a)) {
					throwException(Exc.ARRAYINDEX_EXCEPTION.ordinal());
					break;
				}
				context.heap.set(a, idx, val);
				break;
			case GETARRAY:
				idx = context.stack.pop();
				a = context.stack.pop();
				if (a == 0) {
					throwException(Exc.NULLPOINTER_EXCEPTION.ordinal());
					break;
				}
				if (idx < 0 || idx >= context.heap.getArraySize(a)) {
					throwException(Exc.ARRAYINDEX_EXCEPTION.ordinal());
					break;
				}
				context.stack.push(context.heap.get(a, idx));
				break;
			case ARRAYLENGTH:
				a = context.stack.pop();
				if (a == 0) {
					throwException(Exc.NULLPOINTER_EXCEPTION.ordinal());
					break;
				}
				context.stack.push(context.heap.getArraySize(a));
				break;
			case ADD:
				context.stack.push(context.stack.pop() + context.stack.pop());
				break;
			case SUB:
				context.stack
						.push(-(context.stack.pop() - context.stack.pop()));
				break;
			case MUL:
				context.stack.push(context.stack.pop() * context.stack.pop());
				break;
			case DIV:
				right = context.stack.pop();
				left = context.stack.pop();
				if (right == 0) {
					throwException(Exc.ARITHMETIC_EXCEPTION.ordinal());
					break;
				}
				context.stack.push(left / right);
				break;
			case AND: {
				int n1 = context.stack.pop();
				int n2 = context.stack.pop();
				context.stack.push((n1 != 0 && n2 != 0) ? 1 : 0);
				break;
			}
			case OR: {
				int n1 = context.stack.pop();
				int n2 = context.stack.pop();
				context.stack.push((n1 != 0 || n2 != 0) ? 1 : 0);
				break;
			}
			case EQ:
				context.stack
						.push((context.stack.pop() == context.stack.pop()) ? 1
								: 0);
				break;
			case NEQ:
				context.stack
						.push((context.stack.pop() != context.stack.pop()) ? 1
								: 0);
				break;
			case GT:
				right = context.stack.pop();
				context.stack.push((context.stack.pop() > right) ? 1 : 0);
				break;
			case GE:
				right = context.stack.pop();
				context.stack.push((context.stack.pop() >= right) ? 1 : 0);
				break;
			case LT:
				right = context.stack.pop();
				context.stack.push((context.stack.pop() < right) ? 1 : 0);
				break;
			case LE:
				right = context.stack.pop();
				context.stack.push((context.stack.pop() <= right) ? 1 : 0);
				break;
			case LIT:
				byte hi = code.get(ip++);
				byte lo = code.get(ip++);
				context.stack.push((hi << 8) + (lo & 0xFF));
				break;
			case NOT:
				context.stack.push(context.stack.pop() != 0 ? 0 : 1);
				break;
			case NEGATE:
				context.stack.push(-context.stack.pop());
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
				try {
					context.stack.push(getExtReg(addr, reg));
				} catch (IOException e) {
					throwException(Exc.IO_EXCEPTION.ordinal());
				}
				break;
			}
			case SETEXTREG: {
				short addr = (short) (code.get(ip++) & 0xFF);
				reg = code.get(ip++) & 0xFF;
				try {
					setExtReg(addr, reg, context.stack.pop());
				} catch (IOException e) {
					throwException(Exc.IO_EXCEPTION.ordinal());
				}
				break;
			}
			case BRANCH:
				off = code.get(ip++);
				ip += off;
				break;
			case QBRANCH:
				off = code.get(ip++);
				if (context.stack.pop() == 0)
					ip += off;
				break;
			case DROP:
				context.stack.pop();
				break;
			case PRINT:
				System.out.print(" <" + context.stack.pop() + "> ");
				break;
			case SETTIMER_MS:
				short ms = context.stack.pop();
				short obj = context.stack.pop();
				if (obj == 0) {
					throwException(Exc.NULLPOINTER_EXCEPTION.ordinal());
					break;
				}
				context.setTimer(obj, ms);
				break;
			// case SETTIMER_S:
			// timer = code.get(ip++);
			// context.timers[timer].cnt = context.stack.pop() * 1000;
			// break;
			case STOPTIMER:
				obj = context.stack.pop();
				if (obj == 0) {
					throwException(Exc.NULLPOINTER_EXCEPTION.ordinal());
					break;
				}
				context.stopTimer(obj);
				break;
			case SETTRIGGER:
				short initVal = context.stack.pop();
				obj = context.stack.pop();
				if (obj == 0) {
					throwException(Exc.NULLPOINTER_EXCEPTION.ordinal());
					break;
				}
				context.setTrigger(obj, initVal);
				break;
			case STOPTRIGGER:
				obj = context.stack.pop();
				if (obj == 0) {
					throwException(Exc.NULLPOINTER_EXCEPTION.ordinal());
					break;
				}
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
				int n3 = context.stack.pop();
				int n2 = context.stack.pop();
				int n1 = context.stack.pop();
				if (n3 == 0) {
					throwException(Exc.ARITHMETIC_EXCEPTION.ordinal());
					break;
				}
				context.stack.push(n1 * n2 / n3);
				break;
			}
			case TRAP:
				break;
			default:
				if (c < BC.values().length)
					throw new RuntimeException("unknown bytecode "
							+ BC.values()[c] + " (" + c + ")");
				else
					throw new RuntimeException("unknown bytecode " + c);
			}
		}

	}

	private void _newArr(short sz, boolean objArr)
			throws UncaughtExceptionException {
		if (sz < 0) {
			throwException(Exc.ARRAYINDEX_EXCEPTION.ordinal());
			return;
		}
		int a = context.heap.alloc2(sz, true, objArr);
		if (a == 0) {
			throwException(Exc.OUTOFMEMORY_EXCEPTION.ordinal());
			return;
		}
		context.stack.push(a);
	}

	private void _new(short n) throws UncaughtExceptionException {
		int sz = context.types[n].sz;
		int a = context.heap.alloc2(n, false, false);
		if (a == 0) {
			throwException(Exc.OUTOFMEMORY_EXCEPTION.ordinal());
			return;
		}
		while (sz-- > 0)
			context.heap.set(a, sz, context.stack.pop());
		context.stack.push(a);
	}

	private void setreg(int reg) {
		context.regs[reg] = context.stack.pop();
	}

	private void getreg(int reg) {
		context.stack.push(context.regs[reg]);
	}
}
