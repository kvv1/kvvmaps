package kvv.evlang.rt;

import java.io.IOException;

import kvv.evlang.rt.RTContext.TTEntry;

public abstract class VM {
	public abstract void setExtReg(int addr, int reg, int value)
			throws IOException;

	public abstract int getExtReg(int addr, int reg) throws IOException;

	private Interpreter interpreter;

	private final RTContext cont;

	private final static int STEP = 10;

	public VM(final RTContext cont) throws UncaughtExceptionException {
		this.cont = cont;
	}

	public void init() throws UncaughtExceptionException {
		interpreter = new Interpreter(cont) {
			@Override
			public void setExtReg(int addr, int reg, int value)
					throws IOException {
				VM.this.setExtReg(addr, reg, value);
			}

			@Override
			public int getExtReg(int addr, int reg) throws IOException {
				return VM.this.getExtReg(addr, reg);
			}
		};
		interpreter.interpret(cont.funcs[0]);
		interpreter.interpret(cont.funcs[1]);
	}

	public void loop() {
		for (;;) {
			try {
				Thread.sleep(STEP);
			} catch (InterruptedException e) {
			}
			step();
		}
	}

	public void stepTimers(int step) {
		for (int i = 0; i < cont.timers.length; i++) {
			if (!cont.timers[i].flag) {
				short obj = cont.timers[i].val;
				if (obj != 0) {
					short cnt = cont.heap.get(obj, RTContext.TIMER_CNT_IDX);
					cnt -= step;
					if (cnt <= 0)
						cnt = 0;
					cont.heap.set(obj, RTContext.TIMER_CNT_IDX, cnt);
					if (cnt == 0) {
						cont.currentTT = obj;
						cont.timers[i].val = 0;
						short func = cont.getVMethod(obj,
								RTContext.TIMER_RUN_FUNC_IDX);
						try {
							interpreter.interpret(func, obj);
						} catch (UncaughtExceptionException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		for (int i = 0; i < cont.triggers.length; i++) {
			if (!cont.triggers[i].flag) {
				short obj = cont.triggers[i].val;
				if (obj != 0) {
					short oldVal = cont.heap
							.get(obj, RTContext.TRIGGER_VAL_IDX);
					short func = cont.getVMethod(obj,
							RTContext.TRIGGER_VAL_FUNC_IDX);
					try {
						short newVal = interpreter.eval(func, obj);
						if (newVal != oldVal) {
							cont.heap.set(obj, RTContext.TRIGGER_VAL_IDX,
									newVal);
							func = cont.getVMethod(obj,
									RTContext.TRIGGER_HANDLE_FUNC_IDX);
							interpreter.interpret(func, obj, oldVal, newVal);
						}
					} catch (UncaughtExceptionException e) {
						e.printStackTrace();
					}
				}
			}
		}

		for (TTEntry t : cont.timers) {
			t.flag = false;
		}

		for (TTEntry t : cont.triggers) {
			t.flag = false;
		}

		cont.currentTT = 0;
	}

	private long time = System.currentTimeMillis();

	public void step() {
		long t = time;
		time = System.currentTimeMillis();

		stepTimers((int) (time - t));
	}
}
