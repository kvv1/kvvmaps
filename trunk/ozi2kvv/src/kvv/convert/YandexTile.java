package kvv.convert;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class YandexTile implements MapDescr1 {

	private BufferedImage img;
	private int z;
	private int x;
	private int y;

	public YandexTile(File dir, int tx, int ty, int z) throws IOException {
		img = ImageIO
				.read(new File(dir, "z" + z + "/" + ty + "/" + tx + ".png"));
		this.z = z;
		this.x = tx << 8;
		this.y = ty << 8;
	}

	@Override
	public int getRGB(int x, int y) {
		return img.getRGB(x, y);
	}

	@Override
	public int getWidth() {
		return 256;
	}

	@Override
	public int getHeight() {
		return 256;
	}

	@Override
	public int getMinDestY() {
		double lat = YandexUtils.y2lat_yandex(y, z);
		return (int) YandexUtils.lat2y(lat, z);
	}

	@Override
	public int getMinDestX() {
		return x;
	}

	@Override
	public int getMaxDestY() {
		double lat = YandexUtils.y2lat_yandex(y + 256, z);
		return (int) YandexUtils.lat2y(lat, z);
	}

	@Override
	public int getMaxDestX() {
		return x + 256;
	}

	@Override
	public double getSrcX(int dstX, int dstY) {
		return dstX - x;
	}

	@Override
	public double getSrcY(int dstX, int dstY) {
		double lat = YandexUtils.y2lat(dstY, z);
		return YandexUtils.lat2y_yandex(lat, z) - y;
	}
}
