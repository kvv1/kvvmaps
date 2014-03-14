package kvv.controllers.shared;

import java.io.Serializable;
import java.util.HashMap;

@SuppressWarnings("serial")
public class SystemDescr implements Serializable {
	public ControllerDescr[] controllers;
	public UnitDescr[] units;
	public HashMap<String, ControllerType> controllerTypes;
	public Integer timeZoneOffset;
}
