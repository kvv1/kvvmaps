package kvv.controllers.controller;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import kvv.controllers.protocol.zavorovo.ZavorovoProtocol;
import kvv.controllers.protocol.zavorovo.ZavorovoProtocolOld;

public class ContextListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
	}

	public void contextDestroyed(ServletContextEvent event) {
		ZavorovoProtocol.closeInstance();
		ZavorovoProtocolOld.closeInstance();
	}

}