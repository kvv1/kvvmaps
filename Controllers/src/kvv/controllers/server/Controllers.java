package kvv.controllers.server;

import java.util.HashMap;
import java.util.Map;

import kvv.controllers.shared.Constants;
import kvv.controllers.shared.ControllerDescr;

public class Controllers {
	private final static Map<String, ControllerDescr> nameMap = new HashMap<String, ControllerDescr>();
	private final static Map<Integer, ControllerDescr> addrMap = new HashMap<Integer, ControllerDescr>();

	public static synchronized ControllerDescr get(String name)
			throws Exception {
		ControllerDescr d = nameMap.get(name);
		if (d == null)
			throw new Exception("Контроллер с именем " + name + " не определен");
		return d;
	}

	public static synchronized ControllerDescr get(Integer addr)
			throws Exception {
		ControllerDescr d = addrMap.get(addr);
		if (d == null)
			throw new Exception("Контроллер с адресом " + addr
					+ " не определен");
		return d;
	}

	public static volatile boolean stopped;

	public static volatile Thread thread = new Thread() {

		{
			setDaemon(true);
			setPriority(MIN_PRIORITY);

			try {
				ControllerDescr[] controllers = Utils.jsonRead(
						Constants.controllersFile, ControllerDescr[].class);
				for (ControllerDescr c : controllers) {
					nameMap.put(c.name, c);
					addrMap.put(c.addr, c);
				}
			} catch (Exception e) {
			}

			start();
		}

		@Override
		public void run() {
			while (!stopped) {
				try {
					sleep(5000);
					ControllerDescr[] controllers = Utils.jsonRead(
							Constants.controllersFile, ControllerDescr[].class);

					synchronized (Controllers.class) {
						nameMap.clear();
						addrMap.clear();
						for (ControllerDescr c : controllers) {
							nameMap.put(c.name, c);
							addrMap.put(c.addr, c);
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};
	};

}
