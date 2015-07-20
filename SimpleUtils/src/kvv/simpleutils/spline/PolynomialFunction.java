package kvv.simpleutils.spline;

public class PolynomialFunction {
	/**
	 * The coefficients of the polynomial, ordered by degree -- i.e.,
	 * coefficients[0] is the constant term and coefficients[n] is the
	 * coefficient of x^n where n is the degree of the polynomial.
	 */
	private final double coefficients[];

	/**
	 * Construct a polynomial with the given coefficients. The first element of
	 * the coefficients array is the constant term. Higher degree coefficients
	 * follow in sequence. The degree of the resulting polynomial is the index
	 * of the last non-null element of the array, or 0 if all elements are null.
	 * <p>
	 * The constructor makes a copy of the input array and assigns the copy to
	 * the coefficients property.
	 * </p>
	 * 
	 * @param c
	 *            Polynomial coefficients.
	 * @throws NullArgumentException
	 *             if {@code c} is {@code null}.
	 * @throws NoDataException
	 *             if {@code c} is empty.
	 */
	public PolynomialFunction(double c[]) {
		super();
		int n = c.length;
		while ((n > 1) && (c[n - 1] == 0)) {
			--n;
		}
		this.coefficients = new double[n];
		System.arraycopy(c, 0, this.coefficients, 0, n);
	}

	/**
	 * Compute the value of the function for the given argument.
	 * <p>
	 * The value returned is <br/>
	 * <code>coefficients[n] * x^n + ... + coefficients[1] * x  + coefficients[0]</code>
	 * </p>
	 * 
	 * @param x
	 *            Argument for which the function value should be computed.
	 * @return the value of the polynomial at the given point.
	 * @see UnivariateFunction#value(double)
	 */
	public double value(double x) {
		return evaluate(coefficients, x);
	}

	/**
	 * Uses Horner's Method to evaluate the polynomial with the given
	 * coefficients at the argument.
	 * 
	 * @param coefficients
	 *            Coefficients of the polynomial to evaluate.
	 * @param argument
	 *            Input value.
	 * @return the value of the polynomial.
	 * @throws NoDataException
	 *             if {@code coefficients} is empty.
	 * @throws NullArgumentException
	 *             if {@code coefficients} is {@code null}.
	 */
	protected static double evaluate(double[] coefficients, double argument) {
		int n = coefficients.length;
		double result = coefficients[n - 1];
		for (int j = n - 2; j >= 0; j--) {
			result = argument * result + coefficients[j];
		}
		return result;
	}

	/**
	 * Returns a string representation of the polynomial.
	 * 
	 * <p>
	 * The representation is user oriented. Terms are displayed lowest degrees
	 * first. The multiplications signs, coefficients equals to one and null
	 * terms are not displayed (except if the polynomial is 0, in which case the
	 * 0 constant term is displayed). Addition of terms with negative
	 * coefficients are replaced by subtraction of terms with positive
	 * coefficients except for the first displayed term (i.e. we display
	 * <code>-3</code> for a constant negative polynomial, but
	 * <code>1 - 3 x + x^2</code> if the negative coefficient is not the first
	 * one displayed).
	 * </p>
	 * 
	 * @return a string representation of the polynomial.
	 */
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		if (coefficients[0] == 0.0) {
			if (coefficients.length == 1) {
				return "0";
			}
		} else {
			s.append(toString(coefficients[0]));
		}

		for (int i = 1; i < coefficients.length; ++i) {
			if (coefficients[i] != 0) {
				if (s.length() > 0) {
					if (coefficients[i] < 0) {
						s.append(" - ");
					} else {
						s.append(" + ");
					}
				} else {
					if (coefficients[i] < 0) {
						s.append("-");
					}
				}

				double absAi = Math.abs(coefficients[i]);
				if ((absAi - 1) != 0) {
					s.append(toString(absAi));
					s.append(' ');
				}

				s.append("x");
				if (i > 1) {
					s.append('^');
					s.append(Integer.toString(i));
				}
			}
		}

		return s.toString();
	}

	/**
	 * Creates a string representing a coefficient, removing ".0" endings.
	 * 
	 * @param coeff
	 *            Coefficient.
	 * @return a string representation of {@code coeff}.
	 */
	private static String toString(double coeff) {
		final String c = Double.toString(coeff);
		if (c.endsWith(".0")) {
			return c.substring(0, c.length() - 2);
		} else {
			return c;
		}
	}

	/**
	 * Returns the coefficients of the derivative of the polynomial with the
	 * given coefficients.
	 * 
	 * @param coefficients
	 *            Coefficients of the polynomial to differentiate.
	 * @return the coefficients of the derivative or {@code null} if
	 *         coefficients has length 1.
	 * @throws NoDataException
	 *             if {@code coefficients} is empty.
	 * @throws NullArgumentException
	 *             if {@code coefficients} is {@code null}.
	 */
	protected static double[] differentiate(double[] coefficients) {
		int n = coefficients.length;
		if (n == 1) {
			return new double[] { 0 };
		}
		double[] result = new double[n - 1];
		for (int i = n - 1; i > 0; i--) {
			result[i - 1] = i * coefficients[i];
		}
		return result;
	}

	/**
	 * Returns the derivative as a {@link PolynomialFunction}.
	 * 
	 * @return the derivative polynomial.
	 */
	public PolynomialFunction polynomialDerivative() {
		return new PolynomialFunction(differentiate(coefficients));
	}

}
