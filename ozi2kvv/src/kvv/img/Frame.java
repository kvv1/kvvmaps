package kvv.img;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import kvv.img.Img.DstImg;
import kvv.img.Img.DstImgAdapter;
import kvv.img.Img.SrcImg;
import kvv.img.Img.Transformation;

public class Frame extends JFrame {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		Frame frame = new Frame();
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.setSize(600, 600);
		frame.setVisible(true);
	}

	private BufferedImage imgSrc;
	private BufferedImage imgDst;

	public Frame() {
		try {
			imgSrc = ImageIO.read(new File("c.bmp"));
			imgDst = new BufferedImage(imgSrc.getWidth(), imgSrc.getHeight(),
					imgSrc.getType());

			SrcImg src = new SrcImg() {
				@Override
				public int getRGB(int x, int y) {
					if (x < 0 || x >= imgSrc.getWidth() || y < 0
							|| y >= imgSrc.getHeight())
						return 0;
					return imgSrc.getRGB(x, y);
				}
			};

			DstImg dst = new DstImgAdapter(imgDst);
			
			Img.transform(src, dst, new Transformation() {
				@Override
				public long getX(int x, int y) {
					long _x = (long) x << 32;
					long _y = (long) y << 32;
					return (long) (_x * 0.3 + _y * 0.1);
					// return ((long)x << 32)*10/9;
					// return (long)x << 32;
				}

				@Override
				public long getY(int x, int y) {
					long _x = (long) x << 32;
					long _y = (long) y << 32;
					return (long) (_y * 0.3 - _x * 0.1);
					// return ((long)y << 32)*10/9;
					// return (long)y << 32;
				}
			});

		} catch (IOException e) {
			e.printStackTrace();
		}

		setLayout(new BorderLayout());
		Canvas c = new Canvas() {
			private static final long serialVersionUID = 1L;

			public void paint(java.awt.Graphics g) {
				g.drawImage(imgDst, 0, 0, null);
			}
		};

		add(c);
	}

}
