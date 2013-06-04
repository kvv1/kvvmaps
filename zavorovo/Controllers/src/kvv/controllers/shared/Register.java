package kvv.controllers.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Register implements Serializable {
	public String name;
	public String controller;
	public int addr;
	public int register;
}
