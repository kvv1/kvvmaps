package kvv.controllers.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SystemDescr implements Serializable {
	public ControllerDescr[] controllerDescrs;
	public UnitDescr[] unitDescrs;
	public int timeZoneOffset;
}
