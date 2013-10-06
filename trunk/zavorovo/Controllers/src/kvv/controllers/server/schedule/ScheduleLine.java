package kvv.controllers.server.schedule;

import java.util.Date;

import kvv.controllers.shared.Register;

public class ScheduleLine {
	public Date date;
	public Register register;
	public int value;

	public ScheduleLine(Date date, Register register, int value) {
		this.date = date;
		this.register = register;
		this.value = value;
	}
}