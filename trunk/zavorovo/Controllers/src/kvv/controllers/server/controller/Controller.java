package kvv.controllers.server.controller;

import kvv.controllers.controller.IController;
import kvv.controllers.utils.Constants;
import kvv.controllers.utils.Utils;

public class Controller {

	private static IController controller;

	public static synchronized IController getController() {
		return controller;
	}

	public static synchronized void create() {
		if (controller == null) {
			String busURL = Utils.getProp(Constants.propsFile, "busURL");
			if (busURL == null)
				busURL = "http://localhost/rs485";
			controller = new ControllerWrapperCached(
					new ControllerWrapperLogger(new ControllerWrapperUni(
							new kvv.controllers.controller.Controller(busURL))));
		}
	}

	public static synchronized void close() {
		if (controller != null) {
			controller.close();
			controller = null;
		}
	}
}
