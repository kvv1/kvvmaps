package kvv.controllers.client.page.configuration;

import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.RegisterDescr;
import kvv.controllers.shared.RegisterPresentation;
import kvv.controllers.shared.UnitDescr;

class ErrorLocation {
	ControllerDescr cd;
	RegisterDescr rd;
	UnitDescr ud;
	RegisterPresentation rp;

	public void clear() {
		cd = null;
		rd = null;
		ud = null;
		rp = null;
	}

	public String getLoc() {
		String loc = "";
		if (cd != null)
			loc += "контроллер " + cd.name;
		if (ud != null)
			loc += "страница " + ud.name;
		if (rd != null)
			loc += " регистр " + rd.name;
		if (rp != null)
			loc += " регистр " + rp.name;
		return loc;
	}
}
