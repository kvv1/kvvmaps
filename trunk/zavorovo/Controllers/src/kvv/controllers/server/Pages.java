package kvv.controllers.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import kvv.controllers.server.controller.Controller;
import kvv.controllers.shared.PageDescr;
import kvv.controllers.shared.Register;
import kvv.controllers.utils.Constants;
import kvv.controllers.utils.Utils;
import kvv.evlang.EG1;
import kvv.evlang.ParseException;
import kvv.evlang.impl.ExtRegisterDescr;
import kvv.evlang.rt.RTContext;

public class Pages {
	public static PageDescr[] getPages() throws IOException {
		PageDescr[] pages = Utils.jsonRead(Constants.pagesFile,
				PageDescr[].class);
		for (PageDescr page : pages) {
			try {
				page.script = Utils.readFile(Constants.ROOT + "/scripts/"
						+ page.name);
				page.scriptEnabled = scriptEnabled(page.name);
			} catch (Exception e) {
			}
		}
		return pages;
	}

	public static void saveScript(String pageName, String script)
			throws Exception {
		try {
			new File(Constants.ROOT + "/scripts").mkdir();
			String fileName = Constants.ROOT + "/scripts/" + pageName;
			Utils.writeFile(fileName, script);
			Controller.loadScript(pageName);
		} catch (Throwable e) {
			throw new Exception(e.getMessage());
		}
	}

	public static void enableScript(String pageName, boolean b)
			throws Exception {
		try {
			new File(Constants.ROOT + "/scripts").mkdir();
			Utils.changeProp(Constants.scriptsFile, pageName, "" + b);
			Controller.loadScript(pageName);
		} catch (Throwable e) {
			throw new Exception(e.getMessage());
		}
	}

	public static RTContext parse(String pageName)
			throws FileNotFoundException, ParseException {

		EG1 parser = new EG1(Constants.ROOT + "/scripts/" + pageName) {
			@Override
			protected ExtRegisterDescr getExtRegisterDescr(String extRegName)
					throws ParseException {

				try {
					Register reg = Controllers.getInstance().getRegister(
							extRegName);

					return new ExtRegisterDescr(reg.addr, reg.register);
				} catch (Exception e) {
					throw new ParseException("unknown external register");
				}
			}
		};

		parser.parse();
		return parser.getRTContext();
	}

	public static boolean scriptEnabled(String pageName) {
		return Boolean.valueOf(Utils.getProp(Constants.scriptsFile, pageName));
	}

}
