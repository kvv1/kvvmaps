package kvv.heliostat.shared.spline;

import java.util.Arrays;

public class PolynomialSplineFunction implements Function {
	/**
	 * Spline segment interval delimiters (knots). Size is n + 1 for n segments.
	 */
	private final double knots[];
	/**
	 * The polynomial functions that make up the spline. The first element
	 * determines the value of the spline over the first subinterval, the second
	 * over the second, etc. Spline function values are determined by evaluating
	 * these functions at {@code (x - knot[i])} where i is the knot segment to
	 * which x belongs.
	 */
	private final PolynomialFunction polynomials[];
	/**
	 * Number of spline segments. It is equal to the number of polynomials and
	 * to the number of partition points - 1.
	 */
	private final int n;

	private final double v0;
	private final double d0;

	private final double v1;
	private final double d1;

	/**
	 * Construct a polynomial spline function with the given segment delimiters
	 * and interpolating polynomials. The constructor copies both arrays and
	 * assigns the copies to the knots and polynomials properties, respectively.
	 * 
	 * @param knots
	 *            Spline segment interval delimiters.
	 * @param polynomials
	 *            Polynomial functions that make up the spline.
	 * @throws NullArgumentException
	 *             if either of the input arrays is {@code null}.
	 * @throws NumberIsTooSmallException
	 *             if knots has length less than 2.
	 * @throws DimensionMismatchException
	 *             if {@code polynomials.length != knots.length - 1}.
	 * @throws NonMonotonicSequenceException
	 *             if the {@code knots} array is not strictly increasing.
	 * 
	 */
	public PolynomialSplineFunction(double knots[],
			PolynomialFunction polynomials[]) {
		this.n = knots.length - 1;
		this.knots = new double[n + 1];
		System.arraycopy(knots, 0, this.knots, 0, n + 1);
		this.polynomials = new PolynomialFunction[n];
		System.arraycopy(polynomials, 0, this.polynomials, 0, n);

		v0 = polynomials[n - 1].value(knots[n] - knots[n - 1]);
		d0 = polynomials[n - 1].polynomialDerivative().value(
				knots[n] - knots[n - 1]);
		v1 = polynomials[0].value(0);
		d1 = polynomials[0].polynomialDerivative().value(0);
	}

	/**
	 * Compute the value for the function. See {@link PolynomialSplineFunction}
	 * for details on the algorithm for computing the value of the function.
	 * 
	 * @param v
	 *            Point for which the function value should be computed.
	 * @return the value.
	 * @throws OutOfRangeException
	 *             if {@code v} is outside of the domain of the spline function
	 *             (smaller than the smallest knot point or larger than the
	 *             largest knot point).
	 */
	public double value(double v) {
		int i = Arrays.binarySearch(knots, v);
		if (i < 0) {
			i = -i - 2;
		}
		// This will handle the case where v is the last knot value
		// There are only n-1 polynomials, so if v is the last knot
		// then we will use the last polynomial to calculate the value.

		if (i >= polynomials.length)
			return v0 + d0 * (v - knots[n]);

		if (i < 0)
			return v1 + d1 * (v - knots[0]);

		return polynomials[i].value(v - knots[i]);
	}
}
