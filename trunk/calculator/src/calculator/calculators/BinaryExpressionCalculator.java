package calculator.calculators;

import java.io.IOException;
import java.text.ParseException;

import calculator.Calculator;
import calculator.Context;
import calculator.lex.Lex;
import calculator.lex.Token;

public abstract class BinaryExpressionCalculator extends Calculator {

	@Override
	protected final int evaluateParams(Lex input, Context context)
			throws ParseException, IOException {
		if (input.getLex().type != Token.Type.LEFT_BRACKET)
			throw new ParseException("'(' expected", input.getOffset());

		int left = evaluate(input, context);

		if (input.getLex().type != Token.Type.COMMA)
			throw new ParseException("',' expected", input.getOffset());

		int right = evaluate(input, context);

		if (input.getLex().type != Token.Type.RIGHT_BRACKET)
			throw new ParseException("')' expected", input.getOffset());

		return eval(left, right);
	}

	protected abstract int eval(int left, int right);
}
