package kvv.controllers.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import kvv.controllers.register.AllRegs;
import kvv.controllers.server.utils.Constants;
import kvv.controllers.server.utils.Utils;

import kvv.controllers.shared.ControllerDescr;

public class LogThread extends Thread {
	public static volatile LogThread instance;
	public volatile boolean stopped;

	{
		setDaemon(true);
		setPriority(Thread.MIN_PRIORITY);
		start();
	}

	@Override
	public void run() {
		while (!stopped) {
			try {
				ControllerDescr[] controllers = Utils.jsonRead(
						Constants.controllersFile, ControllerDescr[].class);

				for (ControllerDescr c : controllers) {
					if (c != null) {
						Thread.sleep(10000);
						AllRegs allRegs = ControllersServiceImpl.controller
								.getAllRegs(c.addr);
						List<Integer> regs = new ArrayList<Integer>(
								allRegs.values.keySet());
						Collections.sort(regs);
						// System.out.print("addr:" + c.addr + " ");
						// for(Integer reg : regs)
						// System.out.print(reg + ":" + map.get(reg) + " ");
						// System.out.println();
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
