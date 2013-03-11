package calculator.calculators;


public class DivExpressionCalculator extends BinaryExpressionCalculator {

	@Override
	protected int eval(int left, int right) {
		return left / right;
	}
}
