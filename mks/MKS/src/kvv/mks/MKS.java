package kvv.mks;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import kvv.mks.cloud.Cloud;

@SuppressWarnings("serial")
public class MKS extends JFrame {

	JTabbedPane tabbedPane;
	MainPanel mainPanel;

	public MKS() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		tabbedPane = new JTabbedPane() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isFocusable() {
				return false;
			}
		};

		getContentPane().add(tabbedPane);

		mainPanel = new MainPanel();
		tabbedPane.add("Главная", mainPanel);

		// pack();
		// setResizable(false);
		// setExtendedState(JFrame.MAXIMIZED_BOTH);

		setSize(800, 800);

		setVisible(true);
	}

	private void add(Cloud cloud) {
		mainPanel.add(cloud);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					MKS mks = new MKS();

//					Cloud base = new Cloud(new File(
//							"D:\\Users\\kvv\\Google Drive\\Mks\\skeleton.txt"));
//					base.thinOut(0.2);
//					mks.add(base);

					Cloud scan = new Cloud(new File(
							"c:\\Mks\\mks_cloud.txt"));
					//scan = new Cloud(scan.data, 100000);
					
					mks.add(scan);

//					System.out.println(base.data.size() + " points in base");
					System.out.println(scan.data.size() + " points in scan");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

}
