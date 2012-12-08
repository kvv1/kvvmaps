package ijTest.textditor.controller;

import ijTest.abstracteditor.IController;
import ijTest.abstracteditor.IDocument;
import ijTest.abstracteditor.IView;
import ijTest.textditor.ICursor;
import ijTest.textditor.ITextDocument;
import ijTest.textditor.ITextView;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TextController extends MouseAdapter implements KeyListener,
		IController {

	private ITextDocument document;
	private ITextView view;

	@Override
	public void setDocument(IDocument doc) {
		this.document = (ITextDocument) doc;
	}

	@Override
	public void setView(IView view) {
		this.view = (ITextView) view;
		view.getComponent().addMouseListener(this);
		view.getComponent().addMouseMotionListener(this);
		view.getComponent().addKeyListener(this);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		view.moveCaret(e.getPoint());
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		view.dragCaret(e.getPoint());
	}

	@Override
	public void keyPressed(KeyEvent e) {
		boolean shift = (e.getModifiers() & KeyEvent.SHIFT_MASK) != 0;
		boolean ctrl = (e.getModifiers() & KeyEvent.CTRL_MASK) != 0;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			view.left(shift);
			break;
		case KeyEvent.VK_RIGHT:
			view.right(shift);
			break;
		case KeyEvent.VK_UP:
			view.up(shift);
			break;
		case KeyEvent.VK_DOWN:
			view.down(shift);
			break;
		case KeyEvent.VK_HOME:
			if (ctrl)
				view.homeFile(shift);
			else
				view.homeLine(shift);
			break;
		case KeyEvent.VK_END:
			if (ctrl)
				view.endFile(shift);
			else
				view.endLine(shift);
			break;
		case KeyEvent.VK_C:
			if (ctrl)
				copy();
			break;
		case KeyEvent.VK_INSERT:
			if (shift)
				paste();
			else if (ctrl)
				copy();
			break;
		case KeyEvent.VK_X:
			if (ctrl)
				cut();
			break;
		case KeyEvent.VK_DELETE:
			if (shift)
				cut();
			break;
		case KeyEvent.VK_V:
			if (ctrl)
				paste();
			break;
		case KeyEvent.VK_PAGE_UP:
			view.pageUp(shift);
			break;
		case KeyEvent.VK_PAGE_DOWN:
			view.pageDown(shift);
			break;
		}
	}

	private void paste() {
		ICursor selStart = view.getSelStart();
		ICursor selEnd = view.getSelEnd();
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable contents = clipboard.getContents(null);
		if (contents != null) {
			try {
				String text = (String) (contents
						.getTransferData(DataFlavor.stringFlavor));
				document.replace(selStart, selEnd, text);
			} catch (Exception e) {
                e.printStackTrace();                
			}
		}
		view.ensureVisible();
	}

	private void copy() {
		ICursor selStart = view.getSelStart();
		ICursor selEnd = view.getSelEnd();
		if (selStart.compare(selEnd) != 0) {
			String text = document.getText(selStart, selEnd);
			Clipboard clipboard = Toolkit.getDefaultToolkit()
					.getSystemClipboard();
			clipboard.setContents(new StringSelection(text), null);
		}
		view.ensureVisible();
	}

	private void cut() {
		ICursor selStart = view.getSelStart();
		ICursor selEnd = view.getSelEnd();
		if (selStart.compare(selEnd) != 0) {
			String text = document.getText(selStart, selEnd);
			Clipboard clipboard = Toolkit.getDefaultToolkit()
					.getSystemClipboard();
			clipboard.setContents(new StringSelection(text), null);
			document.replace(selStart, selEnd, "");
		}
		view.ensureVisible();
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
		ICursor selStart = view.getSelStart();
		ICursor selEnd = view.getSelEnd();
		boolean shift = (e.getModifiers() & KeyEvent.SHIFT_MASK) != 0;
		char c = e.getKeyChar();
		if (c != KeyEvent.CHAR_UNDEFINED) {
			switch (c) {
			case KeyEvent.VK_ENTER:
				String indent = "";
				if (selStart.compare(selEnd) == 0) {
					String line = selStart.getLine();
					int i;
					for (i = 0; i < line.length(); i++)
						if (line.charAt(i) != ' ' && line.charAt(i) != '\t')
							break;
					indent = line.substring(0, i);
				}
				document.replace(selStart, selEnd, "\r\n" + indent);
				view.ensureVisible();
				break;
			case KeyEvent.VK_TAB:
				document.replace(selStart, selEnd, "\t");
				view.ensureVisible();
				break;
			case KeyEvent.VK_DELETE:
				if (!shift) {
					if (selStart.compare(selEnd) == 0) {
						if (selEnd.isEOF())
							break;
						selEnd.nextChar();
					}
					document.replace(selStart, selEnd, "");
					view.ensureVisible();
				}
				break;
			case KeyEvent.VK_BACK_SPACE:
				if (selStart.compare(selEnd) == 0) {
					if (selStart.isBOF())
						break;
					selStart.prevChar();
				}
				document.replace(selStart, selEnd, "");
				view.ensureVisible();
				break;
			default:
				if (c >= ' ')
					document.replace(selStart, selEnd, new String(
							new char[] { c }));
				view.ensureVisible();
			}
		}
	}

}
