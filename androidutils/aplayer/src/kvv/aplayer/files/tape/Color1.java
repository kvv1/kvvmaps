package kvv.aplayer.files.tape;

public class Color1 {
	public static int make(int r, int g, int b) {
		return 0xFF000000 + (r << 16) + (g << 8) + b;
	}

	public static int mul(int color, double mult) {
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;

		r *= mult;
		g *= mult;
		b *= mult;

		return make(r, g, b);
	}
	
//	public static int make(int r, int g, int b, ) {
//		new Integer(0).hashCode();
//		return 0xFF000000 + (r << 16) + (g << 8) + b;
//	}
//	
//	
//	
//	
//	public static final int hash(int a) {         
//	      a ^= (a << 13);
//	      a ^= (a >>> 17);        
//	      a ^= (a << 5);
//	      return a;   
//	}
}
