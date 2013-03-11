package kvv.packmap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class PackMap {

	private static final int TAG_DIR = 0;
	private static final int TAG_PACK = 1;

	public static void main(String[] args) throws IOException {

		if (new File(args[0] + ".dir").exists()) {
			System.out.println("repack...");
			if (args.length == 1)
				repack(args[0], args[0]);
			else
				repack(args[0], args[1]);
		} else {
			if (args.length == 1)
				createPackFile1(args[0], args[0]);
			else if (args.length == 2)
				createPackFile1(args[0], args[1]);
		}
	}

	private static void repack(String oldPackName, String packName)
			throws IOException {
		DataOutputStream os = new DataOutputStream(new FileOutputStream(
				packName + ".kvvmap"));

		File dirFile = new File(oldPackName + ".dir");
		os.writeInt((int) (dirFile.length() + 8));
		os.writeInt(TAG_DIR);
		FileInputStream is = new FileInputStream(dirFile);
		copy(is, os);
		is.close();

		File pacFile = new File(oldPackName + ".pac");
		os.writeInt((int) (pacFile.length() + 8));
		os.writeInt(TAG_PACK);
		is = new FileInputStream(pacFile);
		copy(is, os);
		is.close();

		os.close();
	}

	private static void copy(InputStream is, OutputStream os)
			throws IOException {
		byte[] buf = new byte[4096];
		int n;
		while ((n = is.read(buf)) != -1)
			os.write(buf, 0, n);
	}

	static class FileDescr {
		int z;
		int y;
		int x;
		boolean transp;
		String name;
		public int offset;

		public FileDescr(int x, int y, int z, boolean transp, String name) {
			super();
			this.x = x;
			this.y = y;
			this.z = z;
			this.transp = transp;
			this.name = name;
		}

		public long getId() {
			long n = 0;
			n += x;
			n <<= 16;
			n += y;
			n <<= 4;
			n += z;
			n <<= 28;
			n += offset >> 4;
			return n;
		}
	}

	private static void createPackFile1(String dirName, String packName)
			throws IOException {
		File srcDir = new File(dirName);

		List<FileDescr> files = new ArrayList<FileDescr>();

		File[] zoomDirs = srcDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory()
						&& pathname.getName().matches("z\\d+");
			}
		});

		FileFilter ydirFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory()
						&& pathname.getName().matches("\\d+");
			}
		};

		FileFilter imgFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName().toLowerCase();
				return pathname.isFile()
						&& (name.endsWith(".jpg") || name.endsWith(".png") || name
								.endsWith(".gif"));
			}
		};

		for (File zoomDir : zoomDirs) {
			int z = Integer.parseInt(zoomDir.getName().substring(1));
			File[] yDirs = zoomDir.listFiles(ydirFilter);
			for (File yDir : yDirs) {
				int y = Integer.parseInt(yDir.getName());
				File[] imgs = yDir.listFiles(imgFilter);
				for (File img : imgs) {
					String name = img.getName();
					if (name.contains("_."))
						files.add(new FileDescr(Integer.parseInt(name
								.substring(0, name.indexOf("_."))), y, z, true,
								name));
					else
						files.add(new FileDescr(Integer.parseInt(name
								.substring(0, name.indexOf("."))), y, z, false,
								name));
				}
			}
		}

		DataOutputStream out = new DataOutputStream(new FileOutputStream(
				packName + ".kvvmap"));

		out.writeInt(files.size() * 8 + 4 + 4);
		out.writeInt(TAG_DIR);
		for (@SuppressWarnings("unused")
		FileDescr d : files)
			out.writeLong(0);
		out.writeInt(0); // len of data
		out.writeInt(TAG_PACK);

		int off = 0;
		for (FileDescr d : files) {
			while ((off & 15) != 0) {
				out.writeByte(0);
				off++;
			}
			d.offset = off;
			File file = new File(srcDir, "z" + d.z + "/" + d.y + "/" + d.name);
			System.out.println(file.getAbsolutePath());
			int len = (int) file.length();
			out.writeInt(len + 4);
			off += 4;
			out.writeInt(d.transp ? 1 : 0);
			off += 4;
			DataInputStream is = new DataInputStream(new FileInputStream(file));
			byte[] data = new byte[len];
			is.readFully(data);
			is.close();
			out.write(data);
			off += len;
		}
		out.close();

		RandomAccessFile raf = new RandomAccessFile(packName + ".kvvmap", "rw");
		raf.skipBytes(8);
		for (FileDescr d : files)
			raf.writeLong(d.getId());
		raf.writeInt(off + 8);
		raf.close();
	}

}
