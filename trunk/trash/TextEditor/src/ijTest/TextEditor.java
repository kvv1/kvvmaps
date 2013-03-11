package ijTest;

import ijTest.abstracteditor.Frame;
import ijTest.textditor.controller.TextController;
import ijTest.textditor.document.TextDocument;
import ijTest.textditor.view.TextView;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

public class TextEditor extends JFrame {
	private static final long serialVersionUID = 1L;

	private JTabbedPane tabbedPane;

	private TextEditor() {
		setSize(600, 600);
		createMenu();

		tabbedPane = new JTabbedPane() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isFocusable() {
				return false;
			}
		};

		InputMap im = tabbedPane
				.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		im.put(KeyStroke.getKeyStroke("TAB"), "none");

		getContentPane().add(tabbedPane);

		// addPane(new File("D:\\LUDecomposition.java"));
		// addPane(new File("D:\\a.java"));

		setLocation(200, 200);
		setVisible(true);

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				for (int n = 0; n < tabbedPane.getTabCount(); n++) {
					Frame f = (Frame) tabbedPane.getComponentAt(n);
					if (!f.closeWithConfirmation())
						return;
				}
				System.exit(0);
			}
		});
	}

	private Frame getFrame(File file) {
		if (file == null)
			return null;
		for (int n = 0; n < tabbedPane.getTabCount(); n++) {
			Frame f = (Frame) tabbedPane.getComponentAt(n);
			if (file.equals(f.getFile())) {
				return f;
			}
		}
		return null;
	}

	private Frame createFrame() {
		TextView view = new TextView();
		TextController controller = new TextController();

		Frame frame = new Frame(view, controller) {
			private static final long serialVersionUID = 1L;

			@Override
			public void setTitle(String title) {
				int index = tabbedPane.indexOfComponent(this);
				tabbedPane.setTitleAt(index, title);
			}
		};
		tabbedPane.addTab("Tab 1", frame);
		tabbedPane.setSelectedComponent(frame);
		return frame;
	}

	private Frame getCurrentFrame() {
		return (Frame) tabbedPane.getSelectedComponent();
	}

	private void addPane(File file) {
		TextDocument doc;
		try {
			doc = new TextDocument(file);
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(this, "Cannot open file " + file);
			return;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Error reading file " + file);
			return;
		}

		Frame frame = getFrame(file);

		if (frame == null)
			frame = createFrame();

		frame.setDocument(doc);
	}

	private void createMenu() {
		MenuBar menuBar = new MenuBar();

		Menu menu = new Menu("File");
		MenuItem item;

		item = new MenuItem("New");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newFile();
			}
		});
		menu.add(item);

		item = new MenuItem("Open");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				open();
			}
		});
		menu.add(item);

		item = new MenuItem("Save");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		menu.add(item);

		item = new MenuItem("Save as...");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveAs();
			}
		});
		menu.add(item);

		item = new MenuItem("Close");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeWindow();
			}
		});
		menu.add(item);

		menuBar.add(menu);
		setMenuBar(menuBar);
	}

	private void newFile() {
		addPane(null);
	}

	private void open() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Java source", "java");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(TextEditor.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			addPane(chooser.getSelectedFile());
		}
	}

	private void save() {
		Frame frame = getCurrentFrame();
		if (frame != null)
			frame.save();
	}

	private void saveAs() {
		Frame frame = getCurrentFrame();
		if (frame != null)
			frame.saveAs();
	}

	private void closeWindow() {
		Frame frame = getCurrentFrame();
		if (frame != null) {
			boolean close = frame.closeWithConfirmation();
			if (close)
				tabbedPane.remove(frame);
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new TextEditor();
			}
		});
	}
}
