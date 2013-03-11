package ijTest.textditor.view;

import ijTest.abstracteditor.IDocumentListener;
import ijTest.textditor.ITextDocument;
import ijTest.textditor.TextChangedHint;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Coloring implements IDocumentListener {

	private static final Color COLOR_COMMENT = Color.gray;
	private static final Color COLOR_STRING = new Color(0, 128, 0);
	private static final Color COLOR_NUMBER = Color.blue;
	private static final Color COLOR_KEYWORD = new Color(0, 0, 128);

	private static final int BUFFER_INCREMENT = 100;

	private Font fontBold;
	private Font fontItalic;

	private ITextDocument document;

	private int firstInvalidIdx;
	private boolean[] commentFlags = new boolean[BUFFER_INCREMENT];

	private static Set<String> keywords = new TreeSet<String>();
	static {
		keywords.add("abstract");
		keywords.add("continue");
		keywords.add("for");
		keywords.add("new");
		keywords.add("switch");
		keywords.add("assert");
		keywords.add("default");
		keywords.add("goto");
		keywords.add("package");
		keywords.add("synchronized");
		keywords.add("boolean");
		keywords.add("do");
		keywords.add("if");
		keywords.add("private");
		keywords.add("this");
		keywords.add("break");
		keywords.add("double");
		keywords.add("implements");
		keywords.add("protected");
		keywords.add("throw");
		keywords.add("byte");
		keywords.add("else");
		keywords.add("import");
		keywords.add("public");
		keywords.add("throws");
		keywords.add("case");
		keywords.add("enum");
		keywords.add("instanceof");
		keywords.add("return");
		keywords.add("transient");
		keywords.add("catch");
		keywords.add("extends");
		keywords.add("int");
		keywords.add("short");
		keywords.add("try");
		keywords.add("char");
		keywords.add("final");
		keywords.add("interface");
		keywords.add("static");
		keywords.add("void");
		keywords.add("class");
		keywords.add("finally");
		keywords.add("long");
		keywords.add("strictfp");
		keywords.add("volatile");
		keywords.add("const");
		keywords.add("float");
		keywords.add("native");
		keywords.add("super");
		keywords.add("while");
	}

	private static boolean isKeyWord(String str) {
		return keywords.contains(str);
	}

	public Coloring(ITextDocument document, Font fontBold, Font fontItalic) {
		this.document = document;
		this.fontBold = fontBold;
		this.fontItalic = fontItalic;
		document.addListener(this);
	}

	@Override
	public void documentChanged(Object hint) {
		if (hint instanceof TextChangedHint) {
			TextChangedHint tch = (TextChangedHint) hint;
			if (firstInvalidIdx > tch.firstLine)
				firstInvalidIdx = tch.firstLine;
		}
	}

	static Pattern numberPattern = Pattern
			.compile("(\\d+\\.?\\d*[eE]\\-?\\d+[fFdD]?)"
					+ "|(\\d+\\.\\d*([eE]\\-?\\d+)?[fFdD]?)"
					+ "|(0[xX][0-9a-fA-F]+[Ll]?)" + "|(\\d+[LlfFdD]?)");

	private int parseJavaNumber(String str, int i) {
		Matcher matcher;
		if ((matcher = numberPattern.matcher(str)).find(i)) {
			if (matcher.start() != i)
				return i;
			return matcher.end();
		}
		return i;
	}

	boolean processLine(AttributedString as, String str, boolean comment) {

		int i = 0;
		while (i < str.length()) {
			if (comment
					|| (str.charAt(i) == '/' && i < str.length() - 1 && str
							.charAt(i + 1) == '*')) {
				comment = true;
				int i0 = i;
				i = str.indexOf("*/", i);
				if (i == -1)
					i = str.length();
				else {
					i += 2;
					comment = false;
				}
				if (as != null) {
					as.addAttribute(TextAttribute.FOREGROUND, COLOR_COMMENT,
							i0, i);
					as.addAttribute(TextAttribute.FONT, fontItalic, i0, i);
				}
			} else if (str.charAt(i) == '"') {
				int i0 = i;
				i++;
				while (i < str.length()
						&& (str.charAt(i) != '"' || str.charAt(i - 1) == '\\'))
					i++;
				if (i < str.length())
					i++;
				if (as != null)
					as.addAttribute(TextAttribute.FOREGROUND, COLOR_STRING, i0,
							i);
			} else if (str.charAt(i) == '\'') {
				// int i0 = i;
				i++;
				while (i < str.length()
						&& (str.charAt(i) != '\'' || str.charAt(i - 1) == '\\'))
					i++;
				if (i < str.length())
					i++;
				// if (as != null)
				// as.addAttribute(TextAttribute.FOREGROUND, COLOR_NUMBER, i0,
				// i);
			} else if (str.charAt(i) == '/' && i < str.length() - 1
					&& str.charAt(i + 1) == '/') {
				int i0 = i;
				i = str.length();
				if (as != null) {
					as.addAttribute(TextAttribute.FOREGROUND, COLOR_COMMENT,
							i0, i);
					as.addAttribute(TextAttribute.FONT, fontItalic, i0, i);
				}
			} else if (Character.isJavaIdentifierStart(str.charAt(i))) {
				int i0 = i;
				i++;
				while (i < str.length()
						&& Character.isJavaIdentifierPart(str.charAt(i)))
					i++;
				if (as != null && isKeyWord(str.substring(i0, i))) {
					as.addAttribute(TextAttribute.FOREGROUND, COLOR_KEYWORD,
							i0, i);
					as.addAttribute(TextAttribute.FONT, fontBold, i0, i);
				}
			} else if (Character.isDigit(str.charAt(i))) {
				int i0 = i;
				i = parseJavaNumber(str, i);
				if (i == i0)
					i++;
				else if (as != null) {
					as.addAttribute(TextAttribute.FOREGROUND, COLOR_NUMBER, i0,
							i);
				}
			} else {
				i++;
			}
		}

		return comment;
	}

	public void decorateLine(AttributedString as, String str, int lineNo) {
		if (lineNo >= commentFlags.length) {
			int newSize = lineNo + BUFFER_INCREMENT;
			boolean[] t = new boolean[newSize];
			System.arraycopy(commentFlags, 0, t, 0, commentFlags.length);
			commentFlags = t;
		}

		boolean comment = firstInvalidIdx > 0
				&& commentFlags[firstInvalidIdx - 1];

		while (firstInvalidIdx < lineNo) {
			String line = document.getLine(firstInvalidIdx);
			comment = processLine(null, line, comment);
			commentFlags[firstInvalidIdx++] = comment;
		}

		comment = lineNo > 0 && commentFlags[lineNo - 1];
		comment = processLine(as, str, comment);
		if (firstInvalidIdx == lineNo)
			commentFlags[firstInvalidIdx++] = comment;

	}

	public void dispose() {
		document.removeListener(this);
	}
}
