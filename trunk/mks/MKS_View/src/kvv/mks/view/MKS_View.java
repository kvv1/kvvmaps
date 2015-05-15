package kvv.mks.view;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import kvv.mks.Const;
import kvv.mks.cloud.Cloud;
import kvv.mks.view.udp.UDPServer;

@SuppressWarnings("serial")
public class MKS_View extends JFrame {

	MainPanel mainPanel = new MainPanel();

	UDPServer server = new UDPServer(4445) {
		@Override
		protected void received(DatagramPacket datagramPacket) {
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
					datagramPacket.getData()));
		}
	};

	public MKS_View() throws SocketException {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				server.stop();
				System.exit(0);
			}
		});

		getContentPane().add(mainPanel);

		setSize(800, 800);
		setVisible(true);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					MKS_View mks = new MKS_View();
					Cloud scan = new Cloud(new File(Const.ROOT
							+ "\\mks_cloud.txt"));
					scan = new Cloud(scan.data, 100000);

					mks.mainPanel.add(scan);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
