package kvv.controllers.shared;

import java.io.Serializable;
import java.util.HashMap;

@SuppressWarnings("serial")
public class SystemDescr implements Serializable {
	public ControllerDescr[] controllers;
	public UnitDescr[] units;
	public HashMap<String, ControllerType> controllerTypes;
	public Integer timeZoneOffset;
	
	public ControllerDescr getController(int addr) {
		for(ControllerDescr cd : controllers)
			if(cd.addr == addr)
				return cd;
		return null;
	}
	public RegisterDescr getRegister(int addr, int reg) {
		ControllerDescr cd = getController(addr);
		if(cd == null)
			return null;
		for(RegisterDescr rd : cd.registers)
			if(rd.register == reg)
				return rd;
		return null;
	}
	public RegisterDescr getRegister(String name) {
		for(ControllerDescr cd : controllers)
			for(RegisterDescr rd : cd.registers)
				if(rd.name.equals(name))
					return rd;
		return null;
	}
}
