package calculator.lex;

public class Token {
	public enum Type {
		COMMA, LEFT_BRACKET, RIGHT_BRACKET, NUMBER, IDENTIFIER;
	}

	public final Type type;
	public final String param;

	public Token(Type type, String param) {
		this.type = type;
		this.param = param;
	}
}


