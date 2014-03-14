package kvv.controllers.shared;

import java.io.Serializable;

import kvv.controllers.register.ControllerDef;

@SuppressWarnings("serial")
public class ControllerType implements Serializable{
	public ControllerUI ui;
	public ControllerDef def;
}
