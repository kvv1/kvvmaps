/**
 * 
 */
package ijTest.textditor.document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LinesReader {
	private InputStreamReader reader;
	private boolean eof;
	
	public LinesReader(File file) throws FileNotFoundException {
		reader = new FileReader(file);
	}
	
	public String readLine() throws IOException {
		if(eof)
			return null;
		
		StringBuilder input = new StringBuilder();

		int c;

		while (true) {
			switch (c = reader.read()) {
			case -1:
				eof = true;
				return input.toString();
			case '\n':
				return input.toString();
			case '\r':
				continue;
			default:
				input.append((char) c);
				break;
			}
		}
	}
	
	
	void close() throws IOException {
		reader.close();
	}
}