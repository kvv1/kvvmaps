package kvv.controllers.server.controller;

import java.io.IOException;

import kvv.controllers.server.context.Context;
import kvv.controllers.shared.RegisterDescr;
import kvv.exprcalc.EXPR1;
import kvv.exprcalc.ParseException;
import kvv.exprcalc.TokenMgrError;

public class ExprCalculator extends EXPR1 {

	protected final Integer addr;

	public ExprCalculator(Integer addr, String text) {
		super(text);
		this.addr = addr;
	}

	private RegisterDescr getRegDescr(String name) throws ParseException {
		RegisterDescr reg;
		try {
			reg = Context.getInstance().controllers.getRegister(name);
		} catch (Exception e) {
			throw new ParseException(e.getMessage());
		}
		
		if(addr != null && reg.controllerAddr != addr)
			throw new ParseException("Регистр " + name + " не определен на данном контроллере");
		
		return reg;
	}
	
	@Override
	public short getRegValue(String name) throws ParseException {
		RegisterDescr reg = getRegDescr(name);
		try {
			return (short) Context.getInstance().controller.getReg(reg.controllerAddr,
					reg.register);
		} catch (Exception e) {
			throw new ParseException("Не удается прочитать регистр " + name
					+ " (" + e.getMessage() + ")");
		}
	}

	@Override
	public Expr parse() throws ParseException, IOException {
		try {
			return super.parse();
		} catch (TokenMgrError e) {
			throw new ParseException(e.getMessage());
		}
	}

	@Override
	public short getRegValue(int n) {
		throw new IllegalArgumentException();
	}

	@Override
	public short getRegNum(String name) throws ParseException {
		RegisterDescr reg = getRegDescr(name);
		return (short) reg.register;
	}

	@Override
	public String getRegName(int n) {
		throw new IllegalArgumentException();
	}
}
