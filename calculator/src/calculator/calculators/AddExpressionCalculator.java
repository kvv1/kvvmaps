package calculator.calculators;


public class AddExpressionCalculator extends BinaryExpressionCalculator {

	@Override
	protected int eval(int left, int right) {
		return left + right;
	}
}
