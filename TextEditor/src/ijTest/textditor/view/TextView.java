package ijTest.textditor.view;

import ijTest.abstracteditor.IDocument;
import ijTest.abstracteditor.IDocumentListener;
import ijTest.textditor.ICursor;
import ijTest.textditor.ITextDocument;
import ijTest.textditor.ITextView;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.HashSet;

import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

public class TextView extends JComponent implements Scrollable,
		IDocumentListener, ITextView {
	private static final long serialVersionUID = 1L;

	private static final int TAB_SIZE = 4;

	private ITextDocument document;
	private Coloring coloring;
	private ICursor selStart;
	private ICursor selEnd;
	private static Font font = new Font("Courier New", Font.PLAIN, 14);
	private static Font fontBold = new Font("Courier New", Font.BOLD, 14);
	private static Font fontItalic = new Font("Courier New", Font.ITALIC, 14);
	private Dimension fontSize;
	private Rectangle screenRect;
	private int maxWidth = 10;
	private int preferedCol;

	public TextView() {
		HashSet<KeyStroke> managingFocusForwardTraversalKeys = new HashSet<KeyStroke>();
		LookAndFeel.installProperty(this, "focusTraversalKeysForward",
				managingFocusForwardTraversalKeys);
		HashSet<KeyStroke> managingFocusBackwardTraversalKeys = new HashSet<KeyStroke>();
		LookAndFeel.installProperty(this, "focusTraversalKeysBackward",
				managingFocusBackwardTraversalKeys);
		setAutoscrolls(true);
	}

	private void savePreferedCol() {
		preferedCol = col2screen(selEnd.getLine(), selEnd.getColNo());
	}

	private void restorePreferedCol() {
		int c = screen2col(selEnd.getLine(), preferedCol);
		selEnd.set(selEnd.getLineNo(), c);
	}

	public void ensureVisible() {
		int x = col2screen(selEnd.getLine(), selEnd.getColNo())
				* fontSize.width;
		int y = selEnd.getLineNo() * fontSize.height;
		scrollRectToVisible(new Rectangle(x, y, fontSize.width, fontSize.height));
		repaint();
	}

	private int getMaxTextWidth() {
		int w = 0;
		int lines = document.getLineCount();
		for (int i = 0; i < lines; i++) {
			String line = document.getLine(i);
			w = Math.max(w, col2screen(line, line.length()));
		}
		return w;
	}

	private int col2screen(String string, int colNo) {
		int pos = 0;
		for (int i = 0; i < colNo; i++) {
			pos++;
			if (string.charAt(i) == '\t')
				while (pos % TAB_SIZE != 0)
					pos++;
		}
		return pos;
	}

	private int screen2col(String string, int col) {
		int pos = 0;
		int i;
		for (i = 0; i < string.length(); i++) {
			if (pos >= col)
				return i;
			pos++;
			if (string.charAt(i) == '\t')
				while (pos % TAB_SIZE != 0) {
					if (pos >= col)
						return i;
					pos++;
				}
		}
		return i;
	}

	@Override
	public void setDocument(IDocument doc) {
		if (selStart != null)
			selStart.dispose();
		if (selEnd != null)
			selEnd.dispose();
		if (coloring != null)
			coloring.dispose();
		if (document != null)
			document.removeListener(this);

		this.document = (ITextDocument) doc;
		selStart = document.createCursor();
		selEnd = document.createCursor();
		document.addListener(this);
		coloring = new Coloring(this.document, fontBold, fontItalic);
		setSize();
		repaint();
	}

	@Override
	public void documentChanged(Object hint) {
		savePreferedCol();
		setSize();
		repaint();
	}

	private void setSize() {
		maxWidth = getMaxTextWidth() + 1;
		if (fontSize != null) {
			setPreferredSize(new Dimension(maxWidth * fontSize.width, document
					.getLineCount()
					* fontSize.height));
			revalidate();
		}
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		if (orientation == SwingConstants.HORIZONTAL)
			return visibleRect.width;
		else
			return visibleRect.height;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		if (orientation == SwingConstants.HORIZONTAL)
			return fontSize.width;
		else
			return fontSize.height;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.setFont(font);

		if (fontSize == null) {
			fontSize = new Dimension(g.getFontMetrics().getWidths()[0], g
					.getFontMetrics().getHeight());
			setSize();
		}

		Rectangle rect = g.getClipBounds();
		screenRect = new Rectangle(rect);

		int firstLine = rect.y / fontSize.height;
		int y = firstLine * fontSize.height;

		ICursor c = document.createCursor(firstLine, 0);

		for (;;) {
			drawLine(g, y, c);
			y += fontSize.height;
			if (c.isEOF())
				break;
			if (y >= rect.getMaxY())
				break;
			c.nextChar();
		}

		c.dispose();
	}

	private int drawLine(Graphics g, int y, ICursor c) {
		StringBuilder sb = new StringBuilder();
		int sel0 = -1;
		int sel1 = -1;
		Point caretPos = null;

		ICursor selS = selStart;
		ICursor selE = selEnd;
		if (selS.compare(selE) > 0) {
			ICursor t = selS;
			selS = selE;
			selE = t;
		}

		for (;;) {
			if (c.compare(selEnd) == 0) {
				int x = sb.length() * fontSize.width;
				caretPos = new Point(x, y);
			}

			if (sel0 == -1 && c.compare(selS) >= 0)
				sel0 = sb.length();
			if (c.compare(selE) <= 0)
				sel1 = sb.length();

			if (c.isEOL())
				break;

			char c1 = c.getChar();
			if (c1 == '\t') {
				sb.append(' ');
				while (sb.length() % TAB_SIZE != 0)
					sb.append(' ');
			} else {
				sb.append(c1);
			}
			c.nextChar();
		}

		if (sb.length() > 0) {
			String str = sb.toString();

			AttributedString as = new AttributedString(str);
			as.addAttribute(TextAttribute.FONT, font);

			coloring.decorateLine(as, str, c.getLineNo());

			if (sel0 != -1 && sel1 != -1 && sel0 != sel1) {
				as.addAttribute(TextAttribute.BACKGROUND, Color.blue, sel0,
						sel1);
				as.addAttribute(TextAttribute.FOREGROUND, Color.white, sel0,
						sel1);
			}

			g.drawString(as.getIterator(), 0, y
					+ g.getFontMetrics().getAscent());
		}

		if (caretPos != null)
			drawCaret(g, caretPos);

		return sb.length();
	}

	private void drawCaret(Graphics g, Point caretPos) {
		g.fillRect(caretPos.x, caretPos.y, 2, fontSize.height);
	}

	@Override
	public Component getComponent() {
		return this;
	}

	public ICursor getSelStart() {
		return selStart;
	}

	public ICursor getSelEnd() {
		return selEnd;
	}

	private void moveCursor(ICursor cursor, Point point) {
		int line = point.y / fontSize.height;
		if (line >= document.getLineCount())
			line = document.getLineCount();
		if (line < 0)
			line = 0;
		cursor.set(line, 0);
		int col = screen2col(cursor.getLine(), point.x / fontSize.width);
		cursor.set(line, col);
	}

	public void moveCaret(Point point) {
		moveCursor(selStart, point);
		selEnd.set(selStart);
		savePreferedCol();
		ensureVisible();
	}

	public void dragCaret(Point point) {
		moveCursor(selEnd, point);
		savePreferedCol();
		ensureVisible();
	}

	public void pageDown(boolean select) {
		int lines = screenRect.height / fontSize.height;
		int newLine = selEnd.getLineNo() + lines;
		if (newLine >= document.getLineCount())
			newLine = document.getLineCount() - 1;
		selEnd.toLine(newLine);
		restorePreferedCol();
		if (!select)
			selStart.set(selEnd);
		Rectangle r = new Rectangle(screenRect);
		r.translate(0, lines * fontSize.height);
		scrollRectToVisible(r);
		ensureVisible();
	}

	public void pageUp(boolean select) {
		int lines = screenRect.height / fontSize.height;
		int newLine = selEnd.getLineNo() - lines;
		if (newLine < 0)
			newLine = 0;
		selEnd.toLine(newLine);
		restorePreferedCol();
		if (!select)
			selStart.set(selEnd);
		Rectangle r = new Rectangle(screenRect);
		r.translate(0, -lines * fontSize.height);
		scrollRectToVisible(r);
		ensureVisible();
	}

	public void left(boolean select) {
		selEnd.prevChar();
		if (!select)
			selStart.set(selEnd);
		ensureVisible();
		savePreferedCol();
	}

	public void right(boolean select) {
		selEnd.nextChar();
		if (!select)
			selStart.set(selEnd);
		ensureVisible();
		savePreferedCol();
	}

	public void up(boolean select) {
		selEnd.prevLine();
		restorePreferedCol();
		if (!select)
			selStart.set(selEnd);
		ensureVisible();
	}

	public void down(boolean select) {
		selEnd.nextLine();
		restorePreferedCol();
		if (!select)
			selStart.set(selEnd);
		ensureVisible();
	}

	public void homeLine(boolean select) {
		selEnd.toBOL();
		if (!select)
			selStart.set(selEnd);
		ensureVisible();
		savePreferedCol();
	}

	public void homeFile(boolean select) {
		selEnd.toBOF();
		restorePreferedCol();
		if (!select)
			selStart.set(selEnd);
		ensureVisible();
	}

	public void endLine(boolean select) {
		selEnd.toEOL();
		if (!select)
			selStart.set(selEnd);
		ensureVisible();
		savePreferedCol();
	}

	public void endFile(boolean select) {
		selEnd.toEOF();
		restorePreferedCol();
		if (!select)
			selStart.set(selEnd);
		ensureVisible();
	}
}
