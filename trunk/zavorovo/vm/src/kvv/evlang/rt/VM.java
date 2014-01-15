package kvv.evlang.rt;

import kvv.evlang.rt.RTContext.Event;

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

		interpreter.interpret(cont.funcs[0].code);
		interpreter.interpret(cont.funcs[1].code);

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

	private long time = System.currentTimeMillis();

	public void step() {
		long t = time;
		time = System.currentTimeMillis();

		for (kvv.evlang.rt.RTContext.Timer timer : cont.timers) {
			if (timer.cnt > 0) {
				timer.cnt -= (time - t);
				if (timer.cnt <= 0) {
					timer.cnt = 0;
					try {
						interpreter.interpret(timer.handler);
					} catch (UncaughtExceptionException e) {
						e.printStackTrace();
					}
				}
			}
		}

		for (Event event : cont.events) {
			int val;
			try {
				val = interpreter.eval(event.cond);
				if (event.type == RTContext.Event.TYPE_SET) {
					if (event.state == 0 && val != 0)
						interpreter.interpret(event.handler);
				} else if (event.type == RTContext.Event.TYPE_CHANGE) {
					if (event.state != val)
						interpreter.interpret(event.handler);
				}
				event.state = val;
			} catch (UncaughtExceptionException e) {
				e.printStackTrace();
			}
		}
	}
}
