package calculator.calculators;


public class SubExpressionCalculator extends BinaryExpressionCalculator {

	@Override
	protected int eval(int left, int right) {
		return left - right;
	}

}
