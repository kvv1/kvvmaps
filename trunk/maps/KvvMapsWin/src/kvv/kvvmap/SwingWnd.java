package kvv.kvvmap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

public class SwingWnd extends JFrame {
	private static final long serialVersionUID = 1L;

	public static final String ROOT = "d:/kvvmap";
	public static final String MAPS_ROOT = ROOT + "/a_map";
	public static final String PATH_ROOT = ROOT + "/paths";

	private MapViewSw view;

	private SwingWnd() throws IOException {
		setSize(1000, 880);
		setLayout(new BorderLayout());

		view = new MapViewSw(this);

		JPanel panel = new JPanel(new BorderLayout());
		JPanel toolbar = new JPanel(new FlowLayout());
		toolbar.setBackground(Color.GRAY);

		JButton zoomIn = new JButton("+");
		zoomIn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				view.zoomIn();
			}
		});
		toolbar.add(zoomIn);

		JButton zoomOut = new JButton("-");
		zoomOut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				view.zoomOut();
			}
		});
		toolbar.add(zoomOut);

		JButton chPrio = new JButton("<->");
		chPrio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				view.reorderMaps();
			}
		});
		toolbar.add(chPrio);

		JButton dil = new JButton("i-");
		dil.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				view.decInfoLevel();
			}
		});
		toolbar.add(dil);

		JButton iil = new JButton("i+");
		iil.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				view.incInfoLevel();
			}
		});
		toolbar.add(iil);

		JButton targ = new JButton("T");
		targ.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				view.setTarget();
			}
		});
		toolbar.add(targ);

		final JToggleButton large = new JToggleButton("Large");
		
		large.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				view.setLarge(large.isSelected());
			}
		});
		toolbar.add(large);

		panel.add(toolbar, BorderLayout.NORTH);
		panel.add(view);

		getContentPane().add(panel);

		setVisible(true);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				view.saveState();
			}
		});

	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					new SwingWnd();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
}
