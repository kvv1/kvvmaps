package kvv.controllers.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import kvv.controllers.server.context.Context;
import kvv.controllers.server.history.HistoryFile;
import kvv.gwtutils.server.login.LoginServiceImpl;

public class ContextListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {

		Logger.out.println("starting...");

		LoginServiceImpl.users.put("zavorovo", "z");
		LoginServiceImpl.users.put("g_comp", "g");
		LoginServiceImpl.users.put("g_mob", "g");
		LoginServiceImpl.users.put("v_comp", "v");
		LoginServiceImpl.users.put("v_mob", "v");

		Context.start();
		Logger.out.println("started");
	}

	public void contextDestroyed(ServletContextEvent event) {
		Context.stop();
		HistoryFile.stop();
	}
}
