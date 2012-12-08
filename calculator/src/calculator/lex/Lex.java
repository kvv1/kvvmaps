package calculator.lex;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import calculator.lex.Token.Type;

public class Lex {
	
	public Lex(InputStream is) {
		this.is = is;
	}

	public Token getLex() throws IOException, ParseException {
		char c = get();
		while (c <= ' ')
			c = get();
		if (c == '(')
			return new Token(Type.LEFT_BRACKET, null);
		if (c == ')')
			return new Token(Type.RIGHT_BRACKET, null);
		if (c == ',')
			return new Token(Type.COMMA, null);
		if (Character.isLetter(c)) {
			StringBuilder sb = new StringBuilder();
			while (Character.isLetter(c)) {
				sb.append(c);
				c = get();
			}
			unget(c);
			return new Token(Type.IDENTIFIER, sb.toString());
		}
		if (c == '-' || Character.isDigit(c)) {
			StringBuilder sb = new StringBuilder();
			if (c == '-') {
				sb.append(c);
				c = get();
				if (!Character.isDigit(c))
					throw new ParseException("digit expected", offset);
			}

			while (Character.isDigit(c)) {
				sb.append(c);
				c = get();
			}
			unget(c);
			return new Token(Type.NUMBER, sb.toString());
		}
		throw new ParseException("unrecognized symbol", offset);
	}

	private char get() throws ParseException, IOException {
		if (buf != -1) {
			int c = buf;
			buf = -1;
			offset++;
			return (char) c;
		}
		int c = is.read();
		if (c == -1)
			throw new ParseException("unexpected end of file", offset);
		offset++;
		return (char) c;
	}

	private void unget(char c) {
		if (buf != -1)
			throw new IllegalStateException("cannot unget");
		offset--;
		buf = c;
	}

	public int getOffset() {
		return offset;
	}

	private final InputStream is;
	private int buf = -1;
	private int offset;
}