package kvv.evlang;

import java.io.IOException;

public class ExprTest {
	public static void main(String[] args) throws ParseException, IOException {
		
		EXPR expr = new EXPR("1 ? 0 ? 4 : 6привет : 7 ") {
			@Override
			public short getValue(String name) {
				System.out.println("*");
				return 10;
			}
		};
		
		short s = expr.parse();
		
		System.out.println(s);
		
	}
}
