package kvv.exprcalc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import kvv.stdutils.Utils;

public abstract class EXPR1_Base {

	public abstract short getRegValue(String name) throws ParseException;

	public abstract short getRegValue(int n);

	public abstract short getRegNum(String name) throws ParseException;

	public abstract String getRegName(int n);

	enum OP {
		// @formatter:off
		LIT(1, ""),
		REG(2, ""),
		
		COND(3, ""),
		COND1(4, ""),
		
		EQ(5, "=="),
		NEQ(6, "!="),
		
		LT(7, "<"),
		LE(8, "<="),
		GT(9, ">"),
		GE(10, ">="),
		
		PLUS(11, "+"),
		MINUS(12, "-"),
		MUL(13, "*"),
		DIV(14, "/"),
		NOT(15, "!"),
		NEG(16, "-"),

		OR(17, ""),
		AND(18, ""),
		NE0(19, ""),
		PAR(20, "");
		// @formatter:on

		byte code;
		String text;

		OP(int code, String text) {
			this.code = (byte) code;
			this.text = text;
		}
	}

	private static final int MAX_QUICK = 64;

	private static final int REG_MASK = 0x40;
	private static final int LIT_MASK = 0x80;

	public static abstract class Expr {
		public abstract List<Byte> getBytes() throws ParseException;

		public abstract int getValue() throws ParseException;

		public abstract String toStr();
	}

	static class ParExpr extends Expr {

		private final Expr expr;

		public ParExpr(Expr expr) {
			this.expr = expr;
		}

		@Override
		public List<Byte> getBytes() throws ParseException {
			List<Byte> res = new ArrayList<>(expr.getBytes());
			res.add(OP.PAR.code);
			return res;
		}

		@Override
		public int getValue() throws ParseException {
			return expr.getValue();
		}

		@Override
		public String toStr() {
			return "(" + expr.toStr() + ")";
		}

	}

	static class LitExpr extends Expr {

		public final int v;

		public LitExpr(int v) {
			this.v = v;
		}

		@Override
		public List<Byte> getBytes() {
			if (v >= -MAX_QUICK / 2 && v < MAX_QUICK / 2)
				return Arrays
						.asList((byte) (LIT_MASK | ((v + MAX_QUICK / 2) & (MAX_QUICK - 1))));
			return Arrays.asList(OP.LIT.code, (byte) (v >> 8), (byte) v);
		}

		@Override
		public int getValue() {
			return v;
		}

		@Override
		public String toStr() {
			return v + "";
		}
	}

	class RegExpr extends Expr {

		public final String name;

		public RegExpr(String name) {
			this.name = name;
		}

		@Override
		public List<Byte> getBytes() throws ParseException {
			short regNum = getRegNum(name);

			if (regNum < MAX_QUICK)
				return Arrays.asList((byte) (REG_MASK | regNum));

			return Arrays.asList(OP.REG.code, (byte) (regNum >> 8),
					(byte) regNum);
		}

		@Override
		public int getValue() throws ParseException {
			return EXPR1_Base.this.getRegValue(name);
		}

		@Override
		public String toStr() {
			return name;
		}
	}

	static class BinExpr extends Expr {

		public final Expr left;
		public final Expr right;
		public final OP op;

		public BinExpr(Expr expr, OP op) {
			this(null, expr, op);
		}

		public BinExpr(Expr left, Expr right, OP op) {
			this.left = left;
			this.right = right;
			this.op = op;
		}

		public List<Byte> getBytes() throws ParseException {
			List<Byte> res = new ArrayList<>(left.getBytes());
			if (right != null)
				res.addAll(right.getBytes());
			res.add(op.code);
			return res;
		}

		public int getValue() throws ParseException {
			switch (op) {
			case PLUS:
				return left.getValue() + right.getValue();
			case MINUS:
				return left.getValue() - right.getValue();
			case MUL:
				return left.getValue() * right.getValue();
			case DIV:
				return left.getValue() / right.getValue();
			case EQ:
				return left.getValue() == right.getValue() ? 1 : 0;
			case NEQ:
				return left.getValue() != right.getValue() ? 1 : 0;
			case LT:
				return left.getValue() < right.getValue() ? 1 : 0;
			case GT:
				return left.getValue() > right.getValue() ? 1 : 0;
			case LE:
				return left.getValue() <= right.getValue() ? 1 : 0;
			case GE:
				return left.getValue() >= right.getValue() ? 1 : 0;
			case NOT:
				return right.getValue() != 0 ? 1 : 0;
			case NEG:
				return -right.getValue();
			default:
				throw new IllegalArgumentException();
			}
		}

		@Override
		public String toStr() {
			String res = "";
			if (left != null)
				res += left.toStr();
			res += op.text;
			res += right.toStr();
			return res;
		}
	}

	static class CondExpr extends Expr {

		public final Expr expr1;
		public final Expr expr2;
		public final Expr expr3;

		public CondExpr(Expr expr1, Expr expr2, Expr expr3) {
			this.expr1 = expr1;
			this.expr2 = expr2;
			this.expr3 = expr3;
		}

		public List<Byte> getBytes() throws ParseException {
			List<Byte> bytes1 = expr1.getBytes();
			List<Byte> bytes2 = expr2.getBytes();
			List<Byte> bytes3 = expr3.getBytes();

			List<Byte> res = new ArrayList<>(bytes1);
			res.add(OP.COND.code);
			res.add((byte) (bytes2.size() + 2));
			res.addAll(expr2.getBytes());
			res.add(OP.COND1.code);
			res.add((byte) bytes3.size());
			res.addAll(expr3.getBytes());
			return res;
		}

		public int getValue() throws ParseException {
			return expr1.getValue() != 0 ? expr2.getValue() : expr3.getValue();
		}

		@Override
		public String toStr() {
			return expr1.toStr() + "?" + expr2.toStr() + ":" + expr3.toStr();
		}
	}

	static class ORExpr extends Expr {

		public final Expr left;
		public final Expr right;

		public ORExpr(Expr left, Expr right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public List<Byte> getBytes() throws ParseException {
			List<Byte> bytes1 = left.getBytes();
			List<Byte> bytes2 = right.getBytes();

			List<Byte> res = new ArrayList<>(bytes1);
			res.add(OP.OR.code);
			res.add((byte) (bytes2.size() + 1));
			res.addAll(bytes2);
			res.add(OP.NE0.code);

			return res;
		}

		@Override
		public int getValue() throws ParseException {
			if (left.getValue() != 0)
				return 1;
			if (right.getValue() != 0)
				return 1;
			return 0;
		}

		@Override
		public String toStr() {
			return left.toStr() + "||" + right.toStr();
		}
	}

	static class ANDExpr extends Expr {

		public final Expr left;
		public final Expr right;

		public ANDExpr(Expr left, Expr right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public List<Byte> getBytes() throws ParseException {
			List<Byte> bytes1 = left.getBytes();
			List<Byte> bytes2 = right.getBytes();

			List<Byte> res = new ArrayList<>(bytes1);
			res.add(OP.AND.code);
			res.add((byte) (bytes2.size() + 1));
			res.addAll(bytes2);
			res.add(OP.NE0.code);

			return res;
		}

		@Override
		public int getValue() throws ParseException {
			if (left.getValue() == 0)
				return 0;
			if (right.getValue() == 0)
				return 0;
			return 1;
		}

		@Override
		public String toStr() {
			return left.toStr() + "&&" + right.toStr();
		}
	}

	int eval(List<Byte> _bytes) {
		Deque<Integer> stack = new LinkedList<>();

		byte[] bytes = Utils.asByteArray(_bytes);

		int i = 0;

		while (i < bytes.length) {

			byte c = bytes[i++];

			int bcGroup =  c & (~(MAX_QUICK - 1)) & 0xFF;

			if (bcGroup == LIT_MASK) {
				int n = c & (MAX_QUICK - 1);
				stack.push(n - MAX_QUICK / 2);
			} else if (bcGroup == REG_MASK) {
				int n = c & (MAX_QUICK - 1);
				stack.push((int) getRegValue(n));
			} else if (c == OP.LIT.code) {
				int s = bytes[i++] << 8;
				s += (bytes[i++] & 0xFF);
				stack.push(s);
			} else if (c == OP.REG.code) {
				int s = bytes[i++] << 8;
				s += (bytes[i++] & 0xFF);
				stack.push((int) getRegValue(s));
			} else if (c == OP.COND.code) {
				int n = bytes[i++];
				if (stack.pop() == 0)
					i += n;
			} else if (c == OP.COND1.code) {
				int n = bytes[i++];
				i += n + 1;
			} else if (c == OP.EQ.code) {
				int op2 = stack.pop();
				int op1 = stack.pop();
				stack.push(op1 == op2 ? 1 : 0);
			} else if (c == OP.NEQ.code) {
				int op2 = stack.pop();
				int op1 = stack.pop();
				stack.push(op1 == op2 ? 0 : 1);
			} else if (c == OP.LT.code) {
				int op2 = stack.pop();
				int op1 = stack.pop();
				stack.push(op1 < op2 ? 1 : 0);
			} else if (c == OP.LE.code) {
				int op2 = stack.pop();
				int op1 = stack.pop();
				stack.push(op1 <= op2 ? 1 : 0);
			} else if (c == OP.GT.code) {
				int op2 = stack.pop();
				int op1 = stack.pop();
				stack.push(op1 > op2 ? 1 : 0);
			} else if (c == OP.GE.code) {
				int op2 = stack.pop();
				int op1 = stack.pop();
				stack.push(op1 >= op2 ? 1 : 0);
			} else if (c == OP.PLUS.code) {
				stack.push(stack.pop() + stack.pop());
			} else if (c == OP.MINUS.code) {
				int op2 = stack.pop();
				int op1 = stack.pop();
				stack.push(op1 - op2);
			} else if (c == OP.MUL.code) {
				stack.push(stack.pop() * stack.pop());
			} else if (c == OP.DIV.code) {
				int op2 = stack.pop();
				int op1 = stack.pop();
				stack.push(op1 / op2);
			} else if (c == OP.NOT.code) {
				stack.push(stack.pop() == 0 ? 1 : 0);
			} else if (c == OP.NEG.code) {
				stack.push(-stack.pop());
			} else if (c == OP.OR.code) {
				int n = bytes[i++];
				if (stack.pop() != 0) {
					stack.push(1);
					i += n;
				}
			} else if (c == OP.AND.code) {
				int n = bytes[i++];
				if (stack.pop() == 0) {
					stack.push(0);
					i += n;
				}
			} else if (c == OP.NE0.code) {
				stack.push(stack.pop() == 0 ? 0 : 1);
			} else if (c == OP.PAR.code) {
			} else {
				throw new IllegalArgumentException("illegal bytecode " + c);
			}
		}

		int n = stack.pop();
		if (!stack.isEmpty())
			throw new IllegalArgumentException("stack not empty");

		return n;
	}

	Expr decomp(List<Byte> bytes) {

		Deque<Expr> stack = new LinkedList<>();

		int i = 0;

		while (i < bytes.size()) {
			byte c = bytes.get(i++);

			int bcGroup =  c & (~(MAX_QUICK - 1)) & 0xFF;
			
			if (bcGroup == LIT_MASK) {
				int n = c & (MAX_QUICK - 1);
				stack.push(new LitExpr(n - MAX_QUICK / 2));
			} else if (bcGroup == REG_MASK) {
				int n = c & (MAX_QUICK - 1);
				stack.push(new RegExpr(getRegName(n)));
			} else if (c == OP.LIT.code) {
				int s = bytes.get(i++) << 8;
				s += (bytes.get(i++) & 0xFF);
				stack.push(new LitExpr(s));
			} else if (c == OP.REG.code) {
				int s = bytes.get(i++) << 8;
				s += (bytes.get(i++) & 0xFF);
				stack.push(new RegExpr(getRegName(s)));
			} else if (c == OP.COND.code) {
				int n = bytes.get(i++);
				Expr expr2 = decomp(bytes.subList(i, i + n - 2));
				int n1 = bytes.get(i + n - 1);
				Expr expr3 = decomp(bytes.subList(i + n, i + n + n1));
				stack.push(new CondExpr(stack.pop(), expr2, expr3));
				i = i + n + n1;
			} else if (c == OP.EQ.code) {
				Expr op2 = stack.pop();
				Expr op1 = stack.pop();
				stack.push(new BinExpr(op1, op2, OP.EQ));
			} else if (c == OP.NEQ.code) {
				Expr op2 = stack.pop();
				Expr op1 = stack.pop();
				stack.push(new BinExpr(op1, op2, OP.NEQ));
			} else if (c == OP.LT.code) {
				Expr op2 = stack.pop();
				Expr op1 = stack.pop();
				stack.push(new BinExpr(op1, op2, OP.LT));
			} else if (c == OP.LE.code) {
				Expr op2 = stack.pop();
				Expr op1 = stack.pop();
				stack.push(new BinExpr(op1, op2, OP.LE));
			} else if (c == OP.GT.code) {
				Expr op2 = stack.pop();
				Expr op1 = stack.pop();
				stack.push(new BinExpr(op1, op2, OP.GT));
			} else if (c == OP.GE.code) {
				Expr op2 = stack.pop();
				Expr op1 = stack.pop();
				stack.push(new BinExpr(op1, op2, OP.GE));
			} else if (c == OP.PLUS.code) {
				Expr op2 = stack.pop();
				Expr op1 = stack.pop();
				stack.push(new BinExpr(op1, op2, OP.PLUS));
			} else if (c == OP.MINUS.code) {
				Expr op2 = stack.pop();
				Expr op1 = stack.pop();
				stack.push(new BinExpr(op1, op2, OP.MINUS));
			} else if (c == OP.MUL.code) {
				Expr op2 = stack.pop();
				Expr op1 = stack.pop();
				stack.push(new BinExpr(op1, op2, OP.MUL));
			} else if (c == OP.DIV.code) {
				Expr op2 = stack.pop();
				Expr op1 = stack.pop();
				stack.push(new BinExpr(op1, op2, OP.DIV));
			} else if (c == OP.NOT.code) {
				Expr op1 = stack.pop();
				stack.push(new BinExpr(op1, OP.NOT));
			} else if (c == OP.NEG.code) {
				Expr op2 = stack.pop();
				Expr op1 = stack.pop();
				stack.push(new BinExpr(op1, op2, OP.NEG));
			} else if (c == OP.OR.code) {
				int n = bytes.get(i++);
				Expr right = decomp(bytes.subList(i, i + n - 1));
				stack.push(new ORExpr(stack.pop(), right));
				i = i + n;
			} else if (c == OP.AND.code) {
				int n = bytes.get(i++);
				Expr right = decomp(bytes.subList(i, i + n - 1));
				stack.push(new ANDExpr(stack.pop(), right));
				i = i + n;
			} else if (c == OP.PAR.code) {
				stack.push(new ParExpr(stack.pop()));
			} else {
				throw new IllegalArgumentException("illegal bytecode " + c);
			}
		}

		Expr e = stack.pop();
		if (!stack.isEmpty())
			throw new IllegalArgumentException("stack not empty");
		return e;
	}

}
