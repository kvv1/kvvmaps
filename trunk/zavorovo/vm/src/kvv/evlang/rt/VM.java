package kvv.evlang.rt;

import kvv.evlang.rt.RTContext.Event;

public abstract class VM {
	public abstract void setExtReg(int addr, int reg, int value);

	public abstract int getExtReg(int addr, int reg);

	private final Interpreter interpreter = new Interpreter() {
		@Override
		public void setExtReg(int addr, int reg, int value) {
			VM.this.setExtReg(addr, reg, value);
		}

		@Override
		public int getExtReg(int addr, int reg) {
			return VM.this.getExtReg(addr, reg);
		}
	};

	private final RTContext cont;

	private final static int STEP = 10;

	public VM(final RTContext cont) {
		this.cont = cont;
		interpreter.interpret(cont, cont.funcs[0].code);
		interpreter.interpret(cont, cont.funcs[1].code);

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
					interpreter.interpret(cont, timer.handler);
				}
			}
		}

		for (Event event : cont.events) {
			int val = interpreter.eval(cont, event.cond);
			if (event.type == RTContext.Event.TYPE_SET) {
				if (event.state == 0 && val != 0)
					interpreter.interpret(cont, event.handler);
			} else if (event.type == RTContext.Event.TYPE_CHANGE) {
				if (event.state != val)
					interpreter.interpret(cont, event.handler);
			}
			event.state = val;
		}
	}
}
