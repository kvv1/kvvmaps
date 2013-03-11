package calculator.calculators;


public class MultExpressionCalculator extends BinaryExpressionCalculator {

	@Override
	protected int eval(int left, int right) {
		return left * right;
	}
}
