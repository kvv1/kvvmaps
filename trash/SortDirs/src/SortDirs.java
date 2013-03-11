import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;

public class SortDirs {

	public static void main(String[] args) {
		File root = new File(args[0]);
		File newroot = new File(args[1]);
		newroot.mkdir();
		sort(root, newroot);
	}

	private static void sort(File dir, File newdir) {

		File[] files = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return !pathname.isDirectory();
			}
		});

		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		for (File f : files) {
			// f.setLastModified(time += 2000);
			System.out.println(f.getName() + "\t" + f.lastModified());
			copy(f, new File(newdir, f.getName()));
			// f.renameTo(new File(newdir, f.getName()));
		}

		File[] dirs = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});

		Arrays.sort(dirs, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		for (File d : dirs) {
			// d.setLastModified(time += 2000);
			System.out.println(d.getName() + "\t" + d.lastModified());

			File d1 = new File(newdir, d.getName());
			d1.mkdir();
			sort(d, d1);
		}
	}

	private static void copy(File src, File dst) {
		try {
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dst);

			byte[] buf = new byte[1024 * 1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			System.out.println("File copied.");
		} catch (FileNotFoundException ex) {
			System.out
					.println(ex.getMessage() + " in the specified directory.");
			System.exit(0);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
