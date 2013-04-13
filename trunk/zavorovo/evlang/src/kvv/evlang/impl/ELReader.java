package kvv.evlang.impl;

import java.io.IOException;
import java.io.Reader;

public class ELReader extends Reader {

	private final Reader reader;

	public ELReader(Reader reader) {
		this.reader = reader;
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}

	@Override
	public int read(char[] buf, int off, int len) throws IOException {
		int res = reader.read(buf, off, len);
		while (len-- > 0)
			buf[off++] &= 0xFF;
		return res;
	}
}
