package ijTest.abstracteditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public abstract class Frame extends JScrollPane implements IDocumentListener {
	private static final long serialVersionUID = 1L;

	private IView view;

	private IDocument document;
	private IController controller;

	private static int nextUntitledFileNum;
	private int untitledFileNum;

	public Frame(IView view, IController controller) {
		super(view.getComponent());
		this.view = view;
		this.controller = controller;

		controller.setView(view);

		setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		addComponentListener(new ComponentAdapter() {
			public void componentShown(ComponentEvent e) {
				Component component = e.getComponent();
				Container parent = component.getParent();
				if (parent instanceof JTabbedPane) {
					JTabbedPane tabbed = (JTabbedPane) parent;
					if (tabbed.getSelectedComponent() == component)
						if (component instanceof Frame) {
							((Frame) component).view.getComponent()
									.requestFocusInWindow();
						}
				}
			}
		});

		setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
		getViewport().setBackground(Color.white);
	}

	public abstract void setTitle(String name);

	public File getFile() {
		return document.getFile();
	}

	public void setDocument(IDocument doc) {
		if (document != null)
			document.removeListener(this);
		this.document = doc;
		doc.addListener(this);
		view.setDocument(doc);
		controller.setDocument(doc);
		if (doc.getFile() == null)
			untitledFileNum = ++nextUntitledFileNum;
		updateTitle();
	}

	private String getTitle() {
		return document.getFile() == null ? "New file" + untitledFileNum
				: document.getFile().getName();
	}

	public boolean closeWithConfirmation() {
		if (document.getChanged()) {
			String title = getTitle();
			int resp = JOptionPane.showConfirmDialog(this, "Save file " + title
					+ "?", "Save", JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (resp == JOptionPane.CANCEL_OPTION) {
				return false;
			}
			if (resp == JOptionPane.YES_OPTION) {
				return save();
			}
		}
		return true;
	}

	public boolean save() {
		if (document.getFile() != null) {
			doSave(null);
			return true;
		} else {
			return doSaveAs();
		}
	}

	public boolean saveAs() {
		return doSaveAs();
	}
	
	private boolean doSaveAs() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Java source", "java");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (file.exists()) {
				int resp = JOptionPane.showConfirmDialog(this, "File "
						+ file.getAbsolutePath() + " exists. Overwrite?",
						"Save", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (resp != JOptionPane.YES_OPTION)
					return false;
			}
			doSave(file);
			return true;
		} else {
			return false;
		}
	}

	private void doSave(File file) {
		try {
			document.save(file);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Error writing file "
					+ document.getFile());
		}
	}

	private void updateTitle() {
		boolean changed = document.getChanged();
		String title;
		title = (changed ? "*" : "") + getTitle();
		setTitle(title);
	}

	@Override
	public void documentChanged(Object hint) {
		updateTitle();
	}
}
