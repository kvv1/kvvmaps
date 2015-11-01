package kvv.heliostat.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import kvv.heliostat.server.envir.Envir;

public class ContextListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		System.out.println("starting...");
		Heliostat.instance.init();
		System.out.println("started");
	}

	public void contextDestroyed(ServletContextEvent event) {
		Heliostat.instance.close();
		Envir.instance.close();
	}

}