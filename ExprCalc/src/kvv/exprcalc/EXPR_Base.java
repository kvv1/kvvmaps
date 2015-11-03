package kvv.exprcalc;

public class EXPR_Base {
	
	static abstract class Op {
		
		short val;
		String name;
		Op left; 
		Op right;
		
		public Op(short val) {
			this.val = val;
		}

		public Op(String name) {
			this.name = name;
		}

		public Op(Op left, Op right) {
			this.left = left;
			this.right = right;
		}
		
		public Op(Op left) {
			this.left = left;
		}

		protected abstract short getVal();
		
	}

}
