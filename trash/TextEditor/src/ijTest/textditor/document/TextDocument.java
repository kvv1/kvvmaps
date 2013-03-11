package ijTest.textditor.document;

import ijTest.abstracteditor.IDocumentListener;
import ijTest.textditor.ICursor;
import ijTest.textditor.ITextDocument;
import ijTest.textditor.TextChangedHint;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TextDocument implements ITextDocument {
	private File file;
	private boolean changed;
	private Set<IDocumentListener> listeners = new HashSet<IDocumentListener>();
	private List<String> lines;

	public TextDocument(File file) throws IOException {
		this.file = file;
		lines = new ArrayList<String>();
		if (file != null)
			load();
		else
			lines.add("");
	}

	private void load() throws IOException {
		LinesReader reader = null;
		try {
			reader = new LinesReader(file);
			String str;
			while ((str = reader.readLine()) != null)
				lines.add(str);
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	public String getText(ICursor _from, ICursor _to) {
        Cursor from = (Cursor) _from;
        Cursor to = (Cursor) _to;
		if (from.compare(to) > 0) {
			Cursor t = from;
			from = to;
			to = t;
		}

		if (from.line == to.line)
			return lines.get(from.line).substring(from.col, to.col);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(os);
		pw.println(lines.get(from.line).substring(from.col));
		for (int l = from.line + 1; l < to.line; l++) {
			pw.println(lines.get(l));
		}
		pw.print(lines.get(to.line).substring(0, to.col));
		pw.flush();
		return new String(os.toByteArray());
	}

	private void remove(Cursor from, Cursor to) {
		if (from.compare(to) > 0) {
			Cursor t = from;
			from = to;
			to = t;
		}

		lines.set(from.line, lines.get(from.line).substring(0,
				from.col)
				+ lines.get(to.line).substring(to.col));
		for (int l = from.line + 1; l <= to.line; l++)
			lines.remove(from.line + 1);

		int fromLine = from.line;
		int fromCol = from.col;
		int toLine = to.line;
		int toCol = to.col;

		for (Cursor c : cursors) {
			if (c.compare(fromLine, fromCol) > 0
					&& c.compare(toLine, toCol) <= 0) {
				c.line = fromLine;
				c.col = fromCol;
			} else if (c.compare(toLine, toCol) > 0) {
				c.line -= (toLine - fromLine);
				if (c.line == toLine)
					c.col -= toCol;
				if (c.line == fromLine)
					c.col += fromCol;
			}
		}
	}

	private void insert(Cursor from, String text) {
		String[] ls = text.split("\r*\n", -1);

		if (ls.length == 1)
			lines.set(from.line, lines.get(from.line).substring(0,
					from.col)
					+ ls[0] + lines.get(from.line).substring(from.col));
		else {
			String tail = lines.get(from.line).substring(from.col);
			lines.set(from.line, lines.get(from.line).substring(0,
					from.col)
					+ ls[0]);
			for (int l = 1; l < ls.length; l++)
				lines.add(from.line + l, ls[l]);
            String s =lines.get(from.line + ls.length - 1) + tail;
			lines.set(from.line + ls.length - 1, s);
		}

		int fromLine = from.line;
		int fromCol = from.col;

		for (Cursor c : cursors) {
			if (c.compare(fromLine, fromCol) > 0
					|| (c.type == Cursor.Type.RIGHT && c.compare(fromLine,
							fromCol) == 0)) {
				c.line = fromLine + ls.length - 1;
				int col;
				if (ls.length == 1)
					col = fromCol + ls[0].length();
				else
					col = ls[ls.length - 1].length();
				c.col = col;
			}
		}
	}

	public void replace(ICursor _from, ICursor _to, String text) {
        Cursor from = (Cursor) _from;
        Cursor to = (Cursor) _to;
		if (from.compare(to) > 0) {
			Cursor t = from;
			from = to;
			to = t;
		}

        int firstLine = from.line;

		remove(from, to);
		insert(from, text);

		changed = true;

		notifyListeners(new TextChangedHint(firstLine));
	}

	@Override
	public void addListener(IDocumentListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(IDocumentListener listener) {
		listeners.remove(listener);
	}

	private void notifyListeners(Object hint) {
		for (IDocumentListener listener : listeners)
			listener.documentChanged(hint);

	}

	@Override
	public File getFile() {
		return file;
	}

	public boolean getChanged() {
		return changed;
	}

	private Set<Cursor> cursors = new HashSet<Cursor>();

	public void addCursor(Cursor cursor) {
		cursors.add(cursor);
	}

	public void removeCursor(Cursor cursor) {
		cursors.remove(cursor);
	}

	@Override
	public void save(File newFile) throws IOException {
		if(newFile != null)
			file = newFile;
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(file);
			for (int i = 0; i < lines.size(); i++) {
				if (i != 0)
					writer.println();
				writer.print(lines.get(i));
			}
		} finally {
			if (writer != null)
				writer.close();
		}
		changed = false;
		notifyListeners(null);
	}

	public int getLineCount() {
		return lines.size();
	}

	public ICursor createCursor(int line, int col) {
		return new Cursor(this, line, col, Cursor.Type.RIGHT);
	}

	public ICursor createCursor() {
		return new Cursor(this, 0, 0, Cursor.Type.RIGHT);
	}

	public ICursor createCursor(ICursor c) {
		return new Cursor((Cursor)c);
	}

	public String getLine(int line) {
		return lines.get(line);
	}
}
