package kvv.controllers.controller;

import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import kvv.controllers.rs485.PacketTransceiver;

public class ContextListener implements ServletContextListener {

	private TCPModbusServer tcpModbusServer;

	public void contextInitialized(ServletContextEvent event) {
//		try {
//			CommPortIdentifier.getPortIdentifier("COM4");
//		} catch (NoSuchPortException e1) {
//			e1.printStackTrace();
//		}
		BusLogger.log("STARTED");
		try {
			tcpModbusServer = new TCPModbusServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void contextDestroyed(ServletContextEvent event) {
		PacketTransceiver.closeInstance();
		BusLogger.log("STOPPED");
		if (tcpModbusServer != null)
			tcpModbusServer.close();
	}

}