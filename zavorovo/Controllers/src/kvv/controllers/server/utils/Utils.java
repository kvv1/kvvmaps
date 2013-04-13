package kvv.controllers.server.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import com.google.gson.Gson;

public class Utils {
	public static <T> T jsonRead(String file, Class<T> clazz) throws Exception {
		Reader reader = null;
		try {
			reader = new InputStreamReader(new FileInputStream(file),
					"Windows-1251");
			Gson gson = new Gson();
			return gson.fromJson(reader, clazz);
		} finally {
			if (reader != null)
				reader.close();
		}

	}
	public static String readFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = "\r\n";
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}
		reader.close();
		return stringBuilder.toString();
	}
	public static void writeFile(String name, String text) throws IOException {
		FileWriter wr = new FileWriter(name);
		wr.write(text);
		wr.close();
	}

}
