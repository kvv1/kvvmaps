package kvv.evlang.impl;

import java.util.List;

import kvv.evlang.ParseException;
import kvv.evlang.rt.BC;

public class LValue {
	private final Context context;
	private final Expr expr;
	private final String field;
	private final Expr index;

	@Override
	public String toString() {
		return "LValue expr=" + (expr == null ? "null" : expr.type.name)
				+ " field=" + field;
	}

	public LValue(Context context, Expr expr) {
		this.context = context;
		this.expr = expr;
		this.field = null;
		this.index = null;

		// System.out.println(this);
	}

	public Expr getExpr() throws ParseException {
		if (field == null && index == null)
			return expr;

		if (expr == null)
			return new Expr(context, field);

		if (index != null)
			return new Expr(context, expr, index);

		if (field != null)
			return new Expr(context, expr, field);

		throw new IllegalStateException();
	}

	public LValue(Context context, LValue parent, String field,
			List<Expr> argList) throws ParseException {

		// System.out.println("LValue " + parent + " " + field);

		this.context = context;
		this.index = null;

		if (parent == null) {
			if (argList != null) {
				expr = new Expr(context, null, field, argList);
				this.field = null;
			} else {
				expr = null;
				this.field = field;
			}
		} else {
			Expr parentExpr = parent.getExpr();
			if (argList != null) {
				argList.add(0, parentExpr);
				expr = new Expr(context, parentExpr.type, field, argList);
				this.field = null;
			} else {
				expr = parentExpr;
				this.field = field;
			}
		}

		// System.out.println(this);
	}

	public LValue(Context context, LValue parent, Expr index)
			throws ParseException {
		this.context = context;

		this.expr = parent.getExpr();
		this.field = null;
		this.index = index;
	}

	public Code assign(Expr t)
			throws ParseException {
		if (t == null) {
			Expr e = getExpr();
			Code code = e.getCode();
			if (e.type != Type.VOID)
				code.add(BC.DROP);
			return code;
		}

		if (field != null) {
			if (expr == null)
				return Code.assignVar(context, field, t);
			return Code.assignField(context, expr, field, t);
		}
		if (index != null) {
			return Code.assignIndex(context, expr, index, t);
		}

		throw new IllegalStateException();
	}

}
