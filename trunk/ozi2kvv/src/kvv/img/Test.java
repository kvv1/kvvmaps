package kvv.img;

public class Test {
	public static void main(String[] args) {
		long N = 0x100000000l;
		int m = 100;
		
		double x = 0;
		
		for(int i = 0; i < m; i++) {
			x += 1.0/(N-i);
		}
		
		System.out.printf("%d %d %g\n", N, m, x);
	}
}
