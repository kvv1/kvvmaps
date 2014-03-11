package kvv.controllers.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SystemDescr implements Serializable {
	public ControllerDescr[] controllers;
	public UnitDescr[] units;
	public Integer timeZoneOffset;
}
