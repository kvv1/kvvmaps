package calculator;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import calculator.calculators.AddExpressionCalculator;
import calculator.calculators.DivExpressionCalculator;
import calculator.calculators.LetExpressionCalculator;
import calculator.calculators.MultExpressionCalculator;
import calculator.calculators.SubExpressionCalculator;
import calculator.lex.Lex;
import calculator.lex.Token;

public abstract class Calculator {

	protected static final Map<String, Calculator> functions = new HashMap<String, Calculator>();
	static {
		functions.put("add", new AddExpressionCalculator());
		functions.put("sub", new SubExpressionCalculator());
		functions.put("mult", new MultExpressionCalculator());
		functions.put("div", new DivExpressionCalculator());
		functions.put("let", new LetExpressionCalculator());
	}

	public static int evaluate(Lex input, Context context)
			throws ParseException, IOException {
		Token token = input.getLex();
		switch (token.type) {
		case NUMBER:
			try {
				return Integer.parseInt(token.param);
			} catch (NumberFormatException e) {
				throw new ParseException("cannot parse number: " + token.param,
						input.getOffset());
			}
		case IDENTIFIER:
			Calculator e = functions.get(token.param);
			if (e != null)
				return e.evaluateParams(input, context);
			Integer val = context.getVariable(token.param);
			if (val == null)
				throw new ParseException("unknown identifier",
						input.getOffset());
			return val;
		default:
			throw new ParseException("unexpected token", input.getOffset());
		}
	}

	protected abstract int evaluateParams(Lex input, Context context)
			throws ParseException, IOException;

}
