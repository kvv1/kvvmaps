package kvv.heliostat.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ContextListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		System.out.println("starting...");
		Heliostat.startThread();
		System.out.println("started");
	}

	public void contextDestroyed(ServletContextEvent event) {
		Heliostat.stopThread();
	}

}