package kvv.exprcalc;

import java.io.IOException;
import java.util.List;

import kvv.exprcalc.EXPR1_Base.Expr;

public class ExprTest {
	public static void main(String[] args) throws ParseException, IOException {

//		String e = "1 ? (0 ? 4 : привет) : 7 ";
		String e = "(4 - (2 + 3))*r2";
//		String e = "2";
		
		EXPR1 expr1 = new EXPR1(e) {
			@Override
			public short getRegValue(String name) {
				System.out.println(name);
				return 10;
			}

			@Override
			public short getRegNum(String name) throws ParseException {
				return 1;
			}

			@Override
			public String getRegName(int s) {
				System.out.println(s);
				return "name";
			}

			@Override
			public short getRegValue(int n) {
				return 10;
			}
		};
		
		Expr ee = expr1.parse();
		System.out.println(ee.getValue());
		
		List<Byte> bytes = ee.getBytes();
		System.out.println("len = " + bytes.size());
		int v = expr1.eval(bytes);
		System.out.println(v);

		Expr e1 = expr1.decomp(bytes);
		System.out.println(e1.toStr());
	}
}
