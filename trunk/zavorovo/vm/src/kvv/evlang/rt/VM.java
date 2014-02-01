package kvv.evlang.rt;


public abstract class VM {
	public abstract void setExtReg(int addr, int reg, int value);

	public abstract int getExtReg(int addr, int reg);

	private final Interpreter interpreter;

	private final RTContext cont;

	private final static int STEP = 10;

	public VM(final RTContext cont) throws UncaughtExceptionException {
		this.cont = cont;

		interpreter = new Interpreter(cont) {
			@Override
			public void setExtReg(int addr, int reg, int value) {
				VM.this.setExtReg(addr, reg, value);
			}

			@Override
			public int getExtReg(int addr, int reg) {
				return VM.this.getExtReg(addr, reg);
			}
		};

		interpreter.interpret(cont.funcs[0].code, null);
		interpreter.interpret(cont.funcs[1].code, null);
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

	private boolean step(short obj, int step) {
		int cnt = cont.heap.get(obj, RTContext.TIMER_CNT_IDX);
		cnt -= step;
		if (cnt <= 0)
			cnt = 0;
		cont.heap.set(obj, RTContext.TIMER_CNT_IDX, cnt);
		if (cnt == 0) {
			short func = cont.heap.get(obj, RTContext.TIMER_FUNC_IDX);
			try {
				interpreter.interpret(cont.funcs[func].code, obj);
			} catch (UncaughtExceptionException e) {
				e.printStackTrace();
			}
		}
		return cnt == 0;
	}

	public void timersStep(int step) {
		int sz = cont.timers.size();
		boolean gc = false;
		for (int i = 0; i < sz; i++) {
			short obj = cont.timers.getAt(i);
			if (obj != 0) {
				if (step(obj, step)) {
					cont.timers.setAt(i, 0);
					gc = true;
				}
			}
		}
		cont.timers.compact();
		if (gc)
			cont.gc();
	}

	private long time = System.currentTimeMillis();

	public void step() {
		long t = time;
		time = System.currentTimeMillis();

		timersStep((int) (time - t));
	}

}
