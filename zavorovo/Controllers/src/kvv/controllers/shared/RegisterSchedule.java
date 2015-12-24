package kvv.controllers.shared;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class RegisterSchedule implements Serializable {

	public enum State {
		MANUAL, SCHEDULE, EXPRESSION, LOCAL_EXPRESSION
	}

	public ArrayList<ScheduleItem> items = new ArrayList<>();
	public boolean localExpr;
	public ArrayList<Expr> expressions = new ArrayList<>();
	public State state = State.MANUAL;

	public RegisterSchedule() {
	}

	public RegisterSchedule(RegisterSchedule rs) {
		this.localExpr = rs.localExpr;
		this.state = rs.state;
		this.items = new ArrayList<>(rs.items);
		this.expressions = new ArrayList<>(rs.expressions);
	}

	public static class Expr implements Serializable {
		public String expr;
		public String errMsg;

		public Expr() {
		}
		
		public Expr(String expr) {
			this.expr = expr;
		}
	}

	public int getValue(int minutes) {
		if (items.size() == 0)
			return 0;

		int value = items.get(items.size() - 1).value;
		for (ScheduleItem item : items) {
			if (item.minutes > minutes)
				break;
			value = item.value;
		}
		return value;
	}

	public void add(int minutes, int value) {
		for (int i = 0; i < items.size(); i++) {
			ScheduleItem item = items.get(i);
			if (item.minutes == minutes) {
				if (item.value == value)
					items.remove(i);
				else
					item.value = value;
				return;
			}
			if (item.minutes > minutes) {
				items.add(i, new ScheduleItem(minutes, value));
				return;
			}
		}
		items.add(new ScheduleItem(minutes, value));
	}

	public void compact() {
		if (items.size() == 0)
			return;

		int value = items.get(0).value;

		for (int i = 1; i < items.size();) {
			ScheduleItem item = items.get(i);
			if (item.value == value)
				items.remove(i);
			else
				value = items.get(i++).value;
		}

		if (items.size() > 1 && value == items.get(0).value)
			items.remove(0);
	}
}
