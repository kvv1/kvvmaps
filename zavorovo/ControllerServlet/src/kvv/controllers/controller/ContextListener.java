package kvv.controllers.controller;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import kvv.controllers.rs485.Rs485;
import kvv.controllers.utils.Constants;
import kvv.controllers.utils.Props;

public class ContextListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		try {
//			String com = Props.getProp(Constants.propsFile, "COM");
//			if (com != null)
//				Rs485.instance = new Rs485(com);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void contextDestroyed(ServletContextEvent event) {
		if (Rs485.instance != null)
			Rs485.instance.close();
	}

}