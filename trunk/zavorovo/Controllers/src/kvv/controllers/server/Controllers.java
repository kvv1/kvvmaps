package kvv.controllers.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kvv.controllers.server.utils.Constants;
import kvv.controllers.server.utils.Utils;
import kvv.controllers.shared.ControllerDescr;

public class Controllers {
	private final static Map<String, ControllerDescr> nameMap = new HashMap<String, ControllerDescr>();
	private final static Map<Integer, ControllerDescr> addrMap = new HashMap<Integer, ControllerDescr>();

	private final static List<ControllerDescr> controllers = new ArrayList<ControllerDescr>();

	static {
		try {
			ControllerDescr[] controllers1 = Utils.jsonRead(
					Constants.controllersFile, ControllerDescr[].class);

			nameMap.clear();
			addrMap.clear();

			for (ControllerDescr c : controllers1) {
				if (c != null) {
					controllers.add(c);
					nameMap.put(c.name, c);
					addrMap.put(c.addr, c);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ControllerDescr get(String name) throws Exception {
		ControllerDescr d = nameMap.get(name);
		if (d == null)
			throw new Exception("Контроллер с именем " + name + " не определен");
		return d;
	}

	public static ControllerDescr get(int addr) throws Exception {
		ControllerDescr d = addrMap.get(addr);
		if (d == null)
			throw new Exception("Контроллер с адресом " + addr
					+ " не определен");
		return d;
	}

	public static ControllerDescr[] getControllers() {
		return controllers.toArray(new ControllerDescr[0]);
	}
}
