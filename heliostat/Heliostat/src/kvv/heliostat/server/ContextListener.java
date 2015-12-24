package kvv.heliostat.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import kvv.gwtutils.client.login.LoginService;
import kvv.gwtutils.server.login.LoginServiceImpl;
import kvv.heliostat.server.envir.Envir;

public class ContextListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		System.out.println("starting...");
		
		LoginServiceImpl.users.put("u1", "p1");
		LoginServiceImpl.users.put("u2", "p2");

		Heliostat.instance.init();
		System.out.println("started");
	}

	public void contextDestroyed(ServletContextEvent event) {
		Heliostat.instance.close();
		Envir.instance.close();
	}

}