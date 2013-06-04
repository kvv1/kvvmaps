package kvv.controllers.controller;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import kvv.controllers.rs485.PacketTransceiver;

public class ContextListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
	}

	public void contextDestroyed(ServletContextEvent event) {
		PacketTransceiver.closeInstance();
		BusLogger.stopLogger();
	}

}