package kvv.evlang.rt;

import kvv.evlang.impl.Context.EventType;
import kvv.evlang.rt.RTContext.Event;

public class VM {

	private RTContext context;
	private Interpreter interpreter = new Interpreter();

	private final static int STEP = 10;

	public VM(final RTContext cont) {
		this.context = cont;
		interpreter.interpret(cont, cont.funcs[0].code.off);

		for (;;) {
			try {
				Thread.sleep(STEP);
			} catch (InterruptedException e) {
			}

			for (kvv.evlang.rt.RTContext.Timer timer : context.timers) {
				if (timer.cnt > 0) {
					timer.cnt -= STEP;
					if (timer.cnt <= 0) {
						timer.cnt = 0;
						interpreter.interpret(cont, timer.handler.off);
					}
				}
			}

			for (Event event : cont.events) {
				int val = interpreter.eval(cont, event.cond.off);
				if (event.type == EventType.SET) {
					if (event.state == 0 && val != 0)
						interpreter.interpret(cont, event.handler.off);
				} else if (event.type == EventType.CHANGE) {
					if (event.state != val)
						interpreter.interpret(cont, event.handler.off);
				}
				event.state = val;
			}
		}
	}

}
