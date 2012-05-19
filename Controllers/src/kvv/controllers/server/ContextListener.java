package kvv.controllers.server;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import kvv.controllers.server.db.DbConnection;
import kvv.controllers.server.rs485.Controller;
import kvv.controllers.server.rs485.ControllerEmul;
import kvv.controllers.server.rs485.Rs485;
import kvv.controllers.shared.Constants;

public class ContextListener implements ServletContextListener {

	// private ServletContext context = null;

	public void contextInitialized(ServletContextEvent event) {
		// this.context = event.getServletContext();

		// System.out.println(new File("a.txt").getAbsolutePath());

		try {
			File file = new File(Constants.ROOT + "DB");
			String strUrl = "jdbc:derby:" + file.getCanonicalPath()
					+ ";create=true";
			DbConnection.dbConnection = DriverManager.getConnection(strUrl);
			DbConnection.createDB();
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		try {
			boolean emul = Boolean.valueOf(Utils.getProp(Constants.propsFile,
					"emul"));
			if (emul)
				ControllersServiceImpl.controller = new ControllerWrapper(
						new ControllerEmul());
			else
				ControllersServiceImpl.controller = new ControllerWrapper(
						new Controller(new Rs485(Utils.getProp(
								Constants.propsFile, "COM"))));
		} catch (Exception e) {
			e.printStackTrace();
		}

		LogThread.instance = new LogThread();
		
		
		boolean configRouter = Boolean.valueOf(Utils.getProp(Constants.propsFile,
				"configRouter"));
		if(configRouter)
			RouterThread.instance = new RouterThread();

		Scheduler.instance = new Scheduler();

		System.out.println("The Simple Web App. Is Ready");
	}

	@SuppressWarnings("deprecation")
	public void contextDestroyed(ServletContextEvent event) {
		if (Scheduler.instance != null) {
			Scheduler.instance.stopped = true;
			Scheduler.instance.stop();
			Scheduler.instance = null;
		}
		if (LogThread.instance != null) {
			LogThread.instance.stopped = true;
			LogThread.instance.stop();
			LogThread.instance = null;
		}
		if (RouterThread.instance != null) {
			RouterThread.instance.stopped = true;
			RouterThread.instance.stop();
			RouterThread.instance = null;
		}
		
		Controllers.stopped = true;
		Controllers.thread.stop();
		
		try {
			DbConnection.dbConnection.close();
			DbConnection.dbConnection = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("The Simple Web App. Has Been Removed");
		// this.context = null;
	}

}