package kvv.controllers.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import kvv.controllers.server.controller.Controller;

public class ContextListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		Controller.create();
		System.out.println("The Simple Web App. Is Ready");
	}

	@SuppressWarnings("deprecation")
	public void contextDestroyed(ServletContextEvent event) {
		Controller.destroy();
		System.out.println("The Simple Web App. Has Been Removed");
	}

}