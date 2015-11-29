package kvv.controllers.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import kvv.controllers.server.context.Context;

public class ContextListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		//ControllerFactory.create();
		
		System.out.println("starting...");
		Context.start();
		
		System.out.println("The Simple Web App. Is Ready");
	}

	public void contextDestroyed(ServletContextEvent event) {
		Context.stop();
		//ControllerFactory.destroy();
		System.out.println("The Simple Web App. Has Been Removed");
	}

}