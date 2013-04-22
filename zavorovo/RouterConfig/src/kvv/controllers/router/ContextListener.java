package kvv.controllers.router;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ContextListener implements ServletContextListener {

	private static volatile RouterThread routerThread;

	public void contextInitialized(ServletContextEvent event) {
		long routerCheckTime = 1000L * Integer.valueOf(Utils.getProp(
				Constants.propsFile, "routerCheckTimeS"));
		String routerPassword = Utils.getProp(Constants.propsFile,
				"routerPassword");
		String routerPublicIP = Utils.getProp(Constants.propsFile,
				"routerPublicIP");
		String routerLocalIP = Utils.getProp(Constants.propsFile,
				"routerLocalIP");
		String routerGatewayIP = Utils.getProp(Constants.propsFile,
				"routerGatewayIP");
		routerThread = new RouterThread(routerCheckTime, routerPassword,
				routerPublicIP, routerLocalIP, routerGatewayIP);
	}

	public void contextDestroyed(ServletContextEvent event) {
		if (routerThread != null) {
			routerThread.stopThread();
			routerThread = null;
		}
	}

}