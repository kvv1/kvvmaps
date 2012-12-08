package calculator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;

import calculator.lex.Lex;

public class Test {
	public static void main(String[] args) throws ParseException, IOException {
		test("add(1, 2)", 3);
		test("add(1, mult(2, 3))", 7);
		test("mult(add(2, 2), div(9, 3))", 12);
		test("let(a, 5, add(a, a))", 10);
		test("let(a, 5, let(b, mult(a, 10), add(b, a)))", 55);
		test("let(a, let(b, 10, add(b, b)), let(b, 20, add(a, b)))", 40);
	}

	private static void test(String expr, int expected) throws ParseException, IOException {
		int value = Calculator.evaluate(
				new Lex(new ByteArrayInputStream(expr.getBytes())),
				new Context());
		if (expected != value) {
			System.out.println(expr);
			System.out.println("calculated: " + value);
			System.out.println("expected: " + expected);
		}
	}

}
