package calculator.calculators;

import java.io.IOException;
import java.text.ParseException;

import calculator.Calculator;
import calculator.Context;
import calculator.lex.Lex;
import calculator.lex.Token;

public class LetExpressionCalculator extends Calculator {

	@Override
	protected int evaluateParams(Lex input, Context context)
			throws ParseException, IOException {
		if (input.getLex().type != Token.Type.LEFT_BRACKET)
			throw new ParseException("'(' expected", input.getOffset());

		Token varName = input.getLex();
		if (varName.type != Token.Type.IDENTIFIER)
			throw new ParseException("variable name expected", input.getOffset());
		if (functions.get(varName.param) != null)
			throw new ParseException("function name not allowed", input.getOffset());

		if (input.getLex().type != Token.Type.COMMA)
			throw new ParseException("',' expected", input.getOffset());

		int varValue = evaluate(input, context);
		context.putVariable(varName.param, varValue);

		try {
			if (input.getLex().type != Token.Type.COMMA)
				throw new ParseException("',' expected", input.getOffset());

			int value = evaluate(input, context);

			if (input.getLex().type != Token.Type.RIGHT_BRACKET)
				throw new ParseException("')' expected", input.getOffset());

			return value;
		} finally {
			context.popVariable();
		}
	}

}
