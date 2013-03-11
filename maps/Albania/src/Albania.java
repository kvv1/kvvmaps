import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

public class Albania {

	private static final double margin = 0.05;
	private static final double DLON = 1.0 / 8;
	private static final double DLAT = 1.0 / 12;

	public static void main(String[] args) throws IOException {
		File root = new File("C:/albania_map");

		File[] dirs = root.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return Pattern.matches("[A-Z]\\d\\d", name);
			}
		});

		for (File dir : dirs) {
			File[] files = dir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return Pattern.matches("[A-Z][A-Z]\\.gif", name);
				}
			});

			for (File file : files) {
				System.out.println(file.getName());
				String name = file.getName().split("\\.")[0];
				System.out.println(name);
				File propFile = new File(file.getParent(), name + ".cal");
				PrintWriter pw = new PrintWriter(propFile);

				BufferedImage img = ImageIO.read(file);

				int x0 = 10;
				int x1 = img.getWidth() - 10;
				int y0 = 10;
				int y1 = img.getHeight() - 10;

				pw.println("img=" + file.getName());

				double lon0 = Integer.parseInt(dir.getName().substring(1));
				double lat0 = dir.getName().charAt(0) - 'J' + 40;

				double lon = lon0 + (file.getName().charAt(0) - 'A') * DLON;
				double lat = lat0 + (file.getName().charAt(1) - 'A') * DLAT;

				pw.printf("point1=%d,%d,%g,%g\n", x0, y0, lon, lat + DLAT);
				pw.printf("point2=%d,%d,%g,%g\n", x1, y0, lon + DLON, lat
						+ DLAT);
				pw.printf("point3=%d,%d,%g,%g\n", x1, y1, lon + DLON, lat);
				pw.printf("point4=%d,%d,%g,%g\n", x0, y1, lon, lat);

				pw.close();
			}
		}
	}
}
