package ijTest.textditor.document;

import ijTest.textditor.ICursor;

public final class Cursor implements ICursor {
	private TextDocument document;
	int line;
	int col;

	public enum Type {
        LEFT, RIGHT
	}

	Cursor.Type type;

	Cursor(TextDocument document, int line, int col, Cursor.Type type) {
		this.type = type;
		this.line = line;
		this.col = col;
		this.document = document;
		document.addCursor(this);
	}

	Cursor(TextDocument document, Cursor.Type type) {
		this(document, 0, 0, type);
	}

	Cursor(Cursor c, Cursor.Type type) {
		this(c.document, c.line, c.col, type);
	}

	Cursor(Cursor c) {
		this(c.document, c.line, c.col, c.type);
	}

	public void dispose() {
		document.removeCursor(this);
		document = null;
	}

	public int compare(ICursor c) {
        Cursor c1 = (Cursor) c;
		if (line != c1.line)
			return line - c1.line;
		return col - c1.col;
	}

	int compare(int l, int c) {
		if (line != l)
			return line - l;
		return col - c;
	}

	public boolean isEOF() {
		return line == document.getLineCount() - 1
				&& col == document.getLine(line).length();
	}

	public boolean isBOF() {
		return line == 0 && col == 0;
	}

	public boolean isEOL() {
		return col == document.getLine(line).length();
	}

	public void nextChar() {
		if (isEOF())
			return;
		if (col < document.getLine(line).length())
			col++;
		else {
			line++;
			col = 0;
		}
	}

	public void prevChar() {
		if (isBOF())
			return;
		if (col > 0)
			col--;
		else {
			line--;
			col = document.getLine(line).length();
		}
	}

	public void set(ICursor c) {
        Cursor c1 = (Cursor) c;
		line = c1.line;
		col = c1.col;
	}

	public void toBOL() {
		col = 0;
	}

	public String getLine() {
		return document.getLine(line);
	}

	public char getChar() {
		return document.getLine(line).charAt(col);
	}

	public void prevLine() {
		if (line > 0)
			line--;
		col = Math.min(col, document.getLine(line).length());
	}

	public void nextLine() {
		if (line < document.getLineCount() - 1)
			line++;
		col = Math.min(col, document.getLine(line).length());
	}

	public void toEOL() {
		col = document.getLine(line).length();
	}

	public int getColNo() {
		return col;
	}

	public int getLineNo() {
		return line;
	}

	public void set(int line, int col) {
		this.line = line;
		this.col = Math.min(col, document.getLine(line).length());
	}

	public void toEOF() {
		line = document.getLineCount() - 1;
		col = document.getLine(line).length();
	}

	public void toBOF() {
		line = 0;
		col = 0;
	}

	public void toLine(int newLine) {
		line = newLine;
		col = Math.min(col, document.getLine(line).length());
	}
	
}