package kvv.controllers.server.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import kvv.controllers.controller.IController;
import kvv.controllers.server.Pages;
import kvv.controllers.shared.PageDescr;
import kvv.controllers.utils.Constants;
import kvv.controllers.utils.Utils;
import kvv.evlang.rt.Const;
import kvv.evlang.rt.RTContext;
import kvv.evlang.rt.VM;

public abstract class Controller implements IController {

	private static volatile IController controller;

	public static synchronized IController getController() {
		return controller;
	}

	public static synchronized void create() {
		if (controller == null) {
			String busURL = Utils.getProp(Constants.propsFile, "busURL");
			if (busURL == null)
				busURL = "http://localhost/rs485";
			controller = new ControllerWrapperScheduled(
					new ControllerWrapperCached(new ControllerWrapperLogger(
							new ControllerWrapperUni(
									new kvv.controllers.controller.Controller(
											busURL)))));

			thread.start();

			try {
				loadScripts();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static synchronized void destroy() {
		if (controller != null) {
			controller.close();
			controller = null;
		}
	}

	protected volatile boolean stopped;

	protected final IController wrapped;

	public Controller(IController wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public void upload(int addr, byte[] data) throws IOException {
		wrapped.upload(addr, data);
	}

	@Override
	public void close() {
		stopped = true;
		wrapped.close();
	}

	@Override
	public void vmInit(int addr) throws IOException {
		wrapped.vmInit(addr);
	}

	private static Thread thread = new Thread(Controller.class.getSimpleName()
			+ "Thread") {

		{
			setDaemon(true);
			setPriority(Thread.MIN_PRIORITY);
		}

		@Override
		public void run() {
			while (controller != null) {
				try {
					Thread.sleep(100);
					for (VM vm : vms.values())
						vm.step();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};

	private static class VM1 extends VM {

		public VM1(RTContext cont) {
			super(cont);
		}

		@Override
		public void setExtReg(int addr, int reg, int value) {
			try {
				if (controller != null)
					controller.setReg(addr, reg, value);
			} catch (IOException e) {
			}
		}

		@Override
		public int getExtReg(int addr, int reg) {
			try {
				if (controller != null)
					return controller.getReg(addr, reg);
			} catch (IOException e) {
			}
			return Const.INVALID_VALUE;
		}
	}

	private static Map<String, VM> vms = new HashMap<String, VM>();
	private static Map<String, String> vmErrors = new HashMap<String, String>();

	public static synchronized Map<String, String> getVMErrors() {
		return vmErrors;
	}

	public static synchronized void loadScripts() throws IOException {
		vms.clear();
		vmErrors.clear();

		PageDescr[] pages = Pages.getPages();
		for (PageDescr page : pages) {
			if (page.scriptEnabled) {
				try {
					VM vm = new VM1(Pages.parse(page.name));
					vms.put(page.name, vm);
				} catch (Exception e) {
					vmErrors.put(page.name, e.getMessage());
					try {
						Pages.enableScript(page.name, false);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}

	public static synchronized void loadScript(String pageName)
			throws IOException {
		vms.remove(pageName);

		if (Pages.scriptEnabled(pageName)) {
			try {
				VM vm = new VM1(Pages.parse(pageName));
				vms.put(pageName, vm);
				vmErrors.remove(pageName);
			} catch (Exception e) {
				vmErrors.put(pageName, e.getMessage());
				try {
					Pages.enableScript(pageName, false);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public static void unloadScript(String pageName) {
		vms.remove(pageName);
		vmErrors.remove(pageName);
	}

}
