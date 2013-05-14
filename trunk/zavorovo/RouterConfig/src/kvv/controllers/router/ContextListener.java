package kvv.controllers.router;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

		List<String> additionalCommands = new ArrayList<String>();

		try {
			Properties props = Utils.getProps(Constants.propsFile);
			for (int i = 0; i < 100; i++) {
				String str = props.getProperty("routerAdditionalCommand" + i);
				if (str != null)
					additionalCommands.add(str);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		routerThread = new RouterThread(routerCheckTime, routerPassword,
				routerPublicIP, routerLocalIP, routerGatewayIP,
				additionalCommands);
	}

	public void contextDestroyed(ServletContextEvent event) {
		if (routerThread != null) {
			routerThread.stopThread();
			routerThread = null;
		}
	}

}