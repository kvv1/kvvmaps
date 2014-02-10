package kvv.evlang.rt.wnd;

import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class HeapFrame extends JFrame {

	short[] entries;

	JPanel panel = new JPanel() {
		public void paint(java.awt.Graphics _g) {
			Graphics2D g = (Graphics2D) _g;

			synchronized (HeapFrame.this) {
				if (entries == null)
					return;

				for (int i = 0; i < entries.length; i++) {
					int x = i * 8;
					if ((entries[i] & 0x8000) != 0) {
						g.fillRect(x, 0, 8, 8);
					} else {
						g.drawRect(x, 0, 8, 8);
					}
				}

			}
		}
	};

	public synchronized void setData(short[] entries) {
		this.entries = entries;
		invalidate();
	}

	public HeapFrame() {
		setSize(200, 200);
		setVisible(true);

		getContentPane().add(panel);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

}
