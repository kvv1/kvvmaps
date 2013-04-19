package kvv.controllers.controller;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import kvv.controllers.rs485.Rs485;

public class ContextListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
	}

	public void contextDestroyed(ServletContextEvent event) {
		Rs485.closeInstance();
	}

}