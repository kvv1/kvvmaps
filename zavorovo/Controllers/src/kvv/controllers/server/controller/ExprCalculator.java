package kvv.controllers.server.controller;

import java.io.IOException;

import kvv.controllers.server.context.Context;
import kvv.controllers.shared.RegisterDescr;
import kvv.exprcalc.EXPR;
import kvv.exprcalc.ParseException;
import kvv.exprcalc.TokenMgrError;

public class ExprCalculator extends EXPR {

	public ExprCalculator(String text) {
		super(text);
	}

	@Override
	public short getValue(String name) throws ParseException {
		RegisterDescr reg;
		try {
			reg = Context.getInstance().controllers.getRegister(name);
			return (short) Context.getInstance().controller.getReg(reg.addr,
					reg.register);
		} catch (Exception e) {
			throw new ParseException("Не удается прочитать регистр " + name
					+ " (" + e.getMessage() + ")");
		}
	}

	@Override
	public short parse() throws ParseException, IOException {
		try {
			return super.parse();
		} catch (TokenMgrError e) {
			throw new ParseException(e.getMessage());
		}
	}
}
