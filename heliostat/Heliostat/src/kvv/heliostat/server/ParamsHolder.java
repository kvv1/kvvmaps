package kvv.heliostat.server;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import kvv.heliostat.client.dto.Params;
import kvv.stdutils.Utils;

public class ParamsHolder {
	private static final String PARAMS_PATH = "c:/heliostat/params.json";

	public volatile static Params params;
	public static volatile Properties controllerParams = new Properties();
	
	static {
		new File(PARAMS_PATH).getParentFile().mkdirs();
		try {
			params = Utils.jsonRead(PARAMS_PATH, Params.class);
		} catch (IOException e) {
			params = new Params();
		}
		try {
			controllerParams.load(new StringReader(
					ParamsHolder.params.controllerParams));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized static void writeParams() {
		try {
			Utils.jsonWrite(PARAMS_PATH, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			controllerParams.load(new StringReader(
					ParamsHolder.params.controllerParams));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
