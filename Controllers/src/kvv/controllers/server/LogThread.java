package kvv.controllers.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import kvv.controllers.shared.Constants;
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
					Thread.sleep(10000);
					Map<Integer, Integer> map = ControllersServiceImpl.controller
							.getRegs(c.addr);
					List<Integer> regs = new ArrayList<Integer>(map.keySet());
					Collections.sort(regs);
					// System.out.print("addr:" + c.addr + " ");
					// for(Integer reg : regs)
					// System.out.print(reg + ":" + map.get(reg) + " ");
					// System.out.println();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
