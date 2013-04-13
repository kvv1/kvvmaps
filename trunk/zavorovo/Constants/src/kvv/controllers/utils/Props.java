package kvv.controllers.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Props {

	public static Properties getProps(String file) throws IOException {
		Properties props = new Properties();
		FileInputStream is = new FileInputStream(file);
		props.load(is);
		is.close();
		return props;
	}

	public static String getProp(String file, String prop) {
		try {
			Properties props = getProps(file);
			return props.getProperty(prop);
		} catch (Throwable e) {
		}
		return null;
	}

	static public byte fletchSum(byte[] buf, int offset, int len) {
		int S = 0;
		for (; len > 0; len--) {
			byte b = buf[offset++];
			int R = b & 0xFF;
			S += R;
			S = S & 0xFF;
			if (S < R)
				S++;
		}
		// if(S = 255) S = 0;
		return (byte) S;
	}

}
