package kvv.kvvmap.common.maps;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import kvv.kvvmap.common.IntToIntMap;

public class MapDir {
	private final static long MASK = 0xFFFFFFFFF0000000L;
	private final IntToIntMap mapDir = new IntToIntMap();

//	private static int cnt;
	
	public MapDir(File file) throws IOException {
//		Adapter.log("MapDir " + ++cnt);
		FileInputStream is = new FileInputStream(file);
		DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
		try {
			while (true) {
				long n = dis.readLong();
				mapDir.add(n);
			}
		} catch (EOFException e) {
		} finally {
			is.close();
		}
		mapDir.sort();
	}

	public int getOffset(int nx, int ny, int z) {
		long n = 0;
		n += nx;
		n <<= 16;
		n += ny;
		n <<= 4;
		n += z;
		n <<= 28;
		
		int idx1 = mapDir.getIdx(n, MASK);
		int idx = idx1;
		if (idx == -1)
			return -1;
		return (int) ((mapDir.getAt(idx) & ~MASK) << 4);
	}
	
	@Override
	protected void finalize() throws Throwable {
//		Adapter.log("~MapDir " + --cnt);
		super.finalize();
	}
}
