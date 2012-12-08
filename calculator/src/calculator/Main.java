package calculator;

import java.io.ByteArrayInputStream;
import java.text.ParseException;

import calculator.lex.Lex;

public class Main {

	public static void main(String[] args) {
		if (args.length != 1) {
			error("expression required");
			System.exit(1);
		}

		String expr = args[0];

		Lex input = new Lex(new ByteArrayInputStream(expr.getBytes()));

		try {
			int value = Calculator.evaluate(input, new Context());
			System.out.println(value);
		} catch (ParseException e) {
			error(expr, e.getErrorOffset(), e.getMessage());
		} catch (Exception e) {
			error(expr, input.getOffset(), e.getMessage());
		}
	}

	private static void error(String expr, int offset, String string) {
		System.err.println(expr);
		for (int i = 0; i < offset - 1; i++)
			System.err.print(" ");
		System.err.println("^");
		System.err.println(string + " at " + offset);
	}

	private static void error(String string) {
		System.err.println(string);
	}

}
