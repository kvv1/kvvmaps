package kvv.evlang.rt;

import kvv.evlang.rt.RTContext.Event;

public class VM {

	private Interpreter interpreter = new Interpreter();

	private final static int STEP = 10;

	public VM(final RTContext cont) {
		interpreter.interpret(cont, cont.funcs[0].code);
		interpreter.interpret(cont, cont.funcs[1].code);

		for (;;) {
			try {
				Thread.sleep(STEP);
			} catch (InterruptedException e) {
			}

			for (kvv.evlang.rt.RTContext.Timer timer : cont.timers) {
				if (timer.cnt > 0) {
					timer.cnt -= STEP;
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

}
