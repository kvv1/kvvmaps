package kvv.evlang.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kvv.evlang.ParseException;
import kvv.evlang.rt.BC;

public class Funcs {
	public Map<String, Func> funcs = new LinkedHashMap<String, Func>();
	private Map<Integer, Func> funcs1 = new LinkedHashMap<Integer, Func>();

	private final Context context;
	public Func initFunc;

	public Funcs(Context context) {
		this.context = context;

		try {
			initFunc = new Func(context, "<init>", new Locals(), Type.VOID);
			initFunc.code = new Code(context);
			put(initFunc);

			Func mainFunc = new Func(context, "main", new Locals(), Type.VOID);
			put(mainFunc);

			
			Locals locals = new Locals();
			locals.add(new NameAndType("this", Type.NULL));
			locals.add(new NameAndType("ms", Type.INT));
			Func startFunc = new Func(context, "timer:start", locals, Type.VOID) {
				@Override
				public void compileCall(Code code) {
					code.add(BC.SETTIMER_MS);
				}
			};
			startFunc.code = new Code(context);
			put(startFunc);

			locals = new Locals();
			locals.add(new NameAndType("this", Type.NULL));
			Func stopFunc = new Func(context, "timer:stop", locals, Type.VOID) {
				@Override
				public void compileCall(Code code) {
					code.add(BC.STOPTIMER);
				}
			};
			stopFunc.code = new Code(context);
			put(stopFunc);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public Func get(String name) {
		return funcs.get(name);
	}

	public void put(Func func) {
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

	public Func getCreateFunc(Type retType, String name, Locals locals)
			throws ParseException {
		locals.endOfArgs();
		Func func = get(name);
		if (func == null) {
			func = new Func(context, name, locals, retType);
			put(func);
			context.currentFunc = func;
			return func;
		} else if (!func.retType.equals(retType)) {
			context.throwExc(name + " - ?");
		} else if (func.locals.getArgCnt() != locals.getArgCnt())
			context.throwExc(name + " argument number error");

		for (int i = 0; i < func.locals.getArgCnt(); i++)
			locals.get(i).nat.type.checkAssignableTo(context,
					func.locals.get(i).nat.type);

		context.currentFunc = func;
		return func;
	}

	public void dump(Code code) {
		for(Func func : funcs.values())
			func.dump(code);
	}
	
}
