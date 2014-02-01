package kvv.evlang.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kvv.evlang.ParseException;

public class FuncDefList {
	public Map<String, Func> funcs = new LinkedHashMap<String, Func>();
	private Map<Integer, Func> funcs1 = new LinkedHashMap<Integer, Func>();

	private final Context context;

	public FuncDefList(Context context) {
		this.context = context;

		funcs.put("<init>", null);
		funcs.put("<main>", null);
		funcs1.put(0, null);
		funcs1.put(1, null);
	}

	public Func get(String name) {
		return funcs.get(name);
	}

	public void put(Func func) {
		if (func.retType.equals(Type.VOID) && func.name.equals("main")
				&& func.locals.getArgCnt() == 0)
			setMain(func);
		else
			add(func, func.name, funcs.size());
	}

	public int size() {
		return funcs.size();
	}

	public Collection<Func> values() {
		return funcs.values();
	}

	public Func get(int idx) {
		return funcs1.get(idx);
	}

	private void add(Func func, String name, int n) {
		func.n = n;
		funcs1.put(n, func);
		funcs.put(name, func);
	}

	private void setMain(Func func) {
		add(func, "<main>", 1);
	}

	public void setInit(Func func) {
		add(func, "<init>", 0);
	}

	public Func getFunc(String name, List<Expr> argList) throws ParseException {
		Func func = get(name);
		if (func == null)
			context.throwExc(name + " - ?");

		if (func.locals.getArgCnt() != argList.size())
			context.throwExc(name + " argument number error");

		for (int i = 0; i < func.locals.getArgCnt(); i++)
			argList.get(i).type.checkAssignableTo(context,
					func.locals.get(i).nat.type);

		return func;
	}

	public Func getCreateFunc(String name, LocalListDef locals, Type retType)
			throws ParseException {
		locals.endOfArgs();
		Func func = get(name);
		if (func == null) {
			func = new Func(context, name, locals, retType);
			put(func);
			return func;
		} else if (!func.retType.equals(retType)) {
			context.throwExc(name + " - ?");
		} else if (func.locals.getArgCnt() != locals.getArgCnt())
			context.throwExc(name + " argument number error");

		for (int i = 0; i < func.locals.getArgCnt(); i++)
			locals.get(i).nat.type.checkAssignableTo(context,
					func.locals.get(i).nat.type);

		return func;
	}

}
