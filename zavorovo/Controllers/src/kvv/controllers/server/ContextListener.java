package kvv.controllers.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import kvv.controllers.server.context.Context;
import kvv.controllers.server.history.HistoryFile;

public class ContextListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		System.out.println("starting...");
		Context.start();
		System.out.println("started");
	}

	public void contextDestroyed(ServletContextEvent event) {
		Context.stop();
		HistoryFile.stopped = true;
	}

}