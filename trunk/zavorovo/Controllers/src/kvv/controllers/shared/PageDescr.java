package kvv.controllers.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class PageDescr implements Serializable {
	public String name;
	public String[] controllers;
	public String[] registers;
	public String script;
}
