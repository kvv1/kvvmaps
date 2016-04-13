package kvv.heliostat.server;

import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import kvv.gwtutils.server.login.UserData;
import kvv.gwtutils.server.login.UserService;
import kvv.heliostat.server.envir.Envir;
import kvv.simpleutils.src.MD5;

import com.google.gwt.dev.util.collect.HashMap;

public class ContextListener implements ServletContextListener {

	public static UserService userService = new UserService() {
		private Map<String, UserData> users = new HashMap<>();
		{
			users.put("u1", new UserData(MD5.calcMD5("u1" + "p1")));
			users.put("u2", new UserData(MD5.calcMD5("u2" + "p2")));
		}

		@Override
		public UserData find(String name) {
			return users.get(name);
		}
	};
	
	
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