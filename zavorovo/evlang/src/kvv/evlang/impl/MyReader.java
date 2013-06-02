package kvv.evlang.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class MyReader extends Reader {

	private InputStream is;

	public MyReader(String filename) throws FileNotFoundException {
		is = new FileInputStream(filename);
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {

		byte[] buf = new byte[len];
		int n = is.read(buf);
		if (n == -1)
			return -1;

		for (int i = 0; i < n; i++)
			cbuf[off + i] = (char) (buf[i] & 0xFF);

		return n;
	}

	@Override
	public void close() throws IOException {
		is.close();
	}

}
