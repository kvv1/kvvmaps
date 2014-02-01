package kvv.evlang.impl;

import java.util.List;

import kvv.evlang.ParseException;

public class LValue {
	public final Context context;
	public final Expr expr;
	public final String field;

	public LValue(Context context, Expr expr) {
		this.context = context;
		this.expr = expr;
		this.field = null;
	}

	public Expr getExpr() throws ParseException {
		if (field == null)
			return expr;
		else if (expr == null)
			return new Expr(context, field);
		else
			return new Expr(context, expr, field);
	}

	public LValue(Context context, LValue parent, String field,
			List<Expr> argList) throws ParseException {
		this.context = context;

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

	}

}
