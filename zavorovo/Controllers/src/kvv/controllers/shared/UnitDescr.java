package kvv.controllers.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class UnitDescr implements Serializable {
	public String name;
	public String[] controllers;
	public String[] registers;
//	public String script;
//	public boolean scriptEnabled;
}
