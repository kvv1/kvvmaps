package kvv.controllers.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SystemDescr implements Serializable {
	public ControllerDescr[] controllerDescrs;
	public PageDescr[] pageDescrs;
	public int timeZoneOffset;
}
