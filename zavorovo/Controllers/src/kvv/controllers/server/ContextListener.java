package kvv.controllers.server;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import kvv.controllers.server.context.Context;
import kvv.controllers.server.history.HistoryFile;
import kvv.gwtutils.server.login.UserData;
import kvv.gwtutils.server.login.UserService;
import kvv.simpleutils.src.MD5;

public class ContextListener implements ServletContextListener {

	public static UserService userService = new UserService() {
		private Map<String, UserData> users = new HashMap<>();
		{
			users.put("zavorovo", new UserData(MD5.calcMD5("zavorovo" + "z")));
			users.put("g_comp", new UserData(MD5.calcMD5("g_comp" + "g")));
			users.put("g_mob", new UserData(MD5.calcMD5("g_mob" + "g")));
			users.put("v_comp", new UserData(MD5.calcMD5("v_comp" + "v")));
			users.put("v_mob", new UserData(MD5.calcMD5("v_mob" + "v")));
		}

		@Override
		public UserData find(String name) {
			return users.get(name);
		}
	};

	public void contextInitialized(ServletContextEvent event) {

		Logger.out.println("starting...");
		Context.start();
		Logger.out.println("started");
	}

	public void contextDestroyed(ServletContextEvent event) {
		Context.stop();
		HistoryFile.stop();
	}
}
