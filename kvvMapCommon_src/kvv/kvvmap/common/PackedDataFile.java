package kvv.kvvmap.common;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class PackedDataFile {
	private final RandomAccessFile raf;
	private final int off;

	public PackedDataFile(File file, int dataOff) throws FileNotFoundException {
		raf = new RandomAccessFile(file, "r");
		off = dataOff;
	}

	public synchronized InputStream getInputStream(int off) throws IOException {
		return new ByteArrayInputStream(getBytes(off));
	}

	public byte[] getBytes(int off) throws IOException {
		raf.seek(off + this.off);
		int len = raf.readInt();
		byte[] buf = new byte[len];
		raf.readFully(buf);
		return buf;
	}
	
	@Override
	protected void finalize() throws Throwable {
		raf.close();
		super.finalize();
	}

}
