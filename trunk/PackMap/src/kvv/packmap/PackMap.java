package kvv.packmap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class PackMap {

	private static final int FLAG_TRANSPARENT = 1;

	public static void main(String[] args) throws IOException {
		createPackFile(args[0]);
	}

	private static void createPackFile(String packName) throws IOException {
		File curDir = new File(".");

		DataOutputStream outPac = new DataOutputStream(new FileOutputStream(
				packName + ".pac"));
		DataOutputStream outDir = new DataOutputStream(new FileOutputStream(
				packName + ".dir"));

		addFiles(new File(curDir, packName), "", outPac, outDir, new int[] { 0 });

		outPac.close();
		outDir.close();
	}

	private static void addFiles(File dir, String prefix,
			DataOutputStream outPac, DataOutputStream outDir, int[] pos)
			throws IOException {
		File[] ff = dir.listFiles();
		for (File f : ff) {
			String name = f.getName();
			if (f.isFile()) {
				System.out.println(prefix + name);
				writeFile(new File(dir, name), prefix + name, outPac, outDir,
						pos);
			}
			if (f.isDirectory()) {
				addFiles(new File(dir, name), prefix + name + "/", outPac,
						outDir, pos);
			}
		}
	}

	private static void writeFile(File file, String name,
			DataOutputStream outPac, DataOutputStream outDir, int[] pos)
			throws IOException {

		try {
			String[] arr = name.split("[z/_.]");
			int z = Integer.parseInt(arr[1]);
			int ny = Integer.parseInt(arr[2]);
			int nx = Integer.parseInt(arr[3]);
			long n = 0;
			n += nx;
			n <<= 16;
			n += ny;
			n <<= 4;
			n += z;
			n <<= 28;
			n += pos[0] >> 4;
			outDir.writeLong(n);
			
			int len = (int) file.length();
			outPac.writeInt(len + 4);
			pos[0] += 4;

			int flags = name.contains("_") ? FLAG_TRANSPARENT : 0;
			outPac.writeInt(flags);
			pos[0] += 4;
			
			DataInputStream is = new DataInputStream(new FileInputStream(file));
			byte[] data = new byte[len];
			is.readFully(data);
			outPac.write(data);
			is.close();
			pos[0] += len;
			while ((pos[0] & 0xF) != 0) {
				outPac.writeByte(0);
				pos[0]++;
			}
		} catch (NumberFormatException e) {
		}
	}
}
