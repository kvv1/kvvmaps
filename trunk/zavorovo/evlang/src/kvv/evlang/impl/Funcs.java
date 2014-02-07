package kvv.evlang.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kvv.evlang.ParseException;
import kvv.evlang.rt.BC;

public class Funcs {
	public Map<String, Func> funcs = new LinkedHashMap<String, Func>();
	private final Context context;
	public Func initFunc;

	public Funcs(final Context context, boolean root) {
		this.context = context;

		try {
			if (root) {
				initFunc = new Func(context, "<init>", new Locals(), Type.VOID);
				initFunc.code = new Code(context);
				put(initFunc);

				Func mainFunc = new Func(context, "main", new Locals(),
						Type.VOID);
				put(mainFunc);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Func get(String name) {
		return funcs.get(name);
	}

	private void put(Func func) {
		func.n = funcs.size();
		funcs.put(func.name, func);
	}

	public int size() {
		return funcs.size();
	}

	public Collection<Func> values() {
		return funcs.values();
	}

	public Func getFunc(String name, List<Type> argTypes) throws ParseException {
		Func func = get(name);
		if (func == null)
			context.throwWatIsIt(name);

		if (func.locals.getArgCnt() != argTypes.size())
			context.throwArgNumErr(name);

		for (int i = 0; i < func.locals.getArgCnt(); i++)
			argTypes.get(i).checkAssignableTo(context,
					func.locals.get(i).nat.type);

		return func;
	}

	public Func getCreateFunc(Type retType, String name, Locals locals,
			boolean shouldExist) throws ParseException {
		locals.endOfArgs();
		Func func = get(name);
		if (func == null) {
			if (shouldExist)
				context.throwWatIsIt(name);

			func = new Func(context, name, locals, retType);
			put(func);
			context.currentFunc = func;
			return func;
		}

		if (!func.retType.equals(retType))
			context.throwWatIsIt(name);
		else if (func.locals.getArgCnt() != locals.getArgCnt())
			context.throwArgNumErr(name);

		for (int i = 0; i < func.locals.getArgCnt(); i++)
			locals.get(i).nat.type.checkAssignableTo(context,
					func.locals.get(i).nat.type);

		context.currentFunc = func;
		return func;
	}

	public void dump(Code code) {
		for (Func func : funcs.values())
			func.dump(code);
	}

	public void addAll(Funcs funcs2, Type thisType) {
		for (Func f : funcs2.values()) {
			Func f1 = new Func(f, thisType);
			funcs.put(f1.name, f1);
		}
	}

	public short[] getVTable() {
		short[] methods = new short[funcs.size()];
		for (Func f : funcs.values())
			methods[f.n] = f.getOff();
		return methods;
	}

	public void createTimerFuncs() throws ParseException {
		Locals locals;
		Func func;

		locals = new Locals();
		locals.add(new NameAndType("this", Type.NULL));
		func = new Func(context, "run", locals, Type.VOID);
		put(func);

		locals = new Locals();
		locals.add(new NameAndType("this", Type.NULL));
		locals.add(new NameAndType("ms", Type.INT));
		func = new Func(context, "start", locals, Type.VOID) {
			@Override
			public Code getVCallCode() {
				Code code = new Code(context);
				code.add(BC.SETTIMER_MS);
				return code;
			}

			@Override
			public boolean isDefined() {
				return true;
			}
		};
		func.code = new Code(context);
		put(func);

		locals = new Locals();
		locals.add(new NameAndType("this", Type.NULL));
		func = new Func(context, "stop", locals, Type.VOID) {
			@Override
			public Code getVCallCode() {
				Code code = new Code(context);
				code.add(BC.STOPTIMER);
				return code;
			}

			@Override
			public boolean isDefined() {
				return true;
			}
		};
		func.code = new Code(context);
		put(func);
	}

	public void createTriggerFuncs() throws ParseException {
		Locals locals;
		Func func;

		locals = new Locals();
		locals.add(new NameAndType("this", Type.NULL));
		func = new Func(context, "value", locals, Type.INT);
		put(func);
		
		locals = new Locals();
		locals.add(new NameAndType("this", Type.NULL));
		locals.add(new NameAndType("oldValue", Type.INT));
		locals.add(new NameAndType("newValue", Type.INT));
		func = new Func(context, "handle", locals, Type.VOID);
		put(func);

		locals = new Locals();
		locals.add(new NameAndType("this", Type.NULL));
		locals.add(new NameAndType("initValue", Type.INT));
		func = new Func(context, "start", locals, Type.VOID) {
			@Override
			public Code getVCallCode() {
				Code code = new Code(context);
				code.add(BC.SETTRIGGER);
				return code;
			}

			@Override
			public boolean isDefined() {
				return true;
			}
		};
		func.code = new Code(context);
		put(func);

		locals = new Locals();
		locals.add(new NameAndType("this", Type.NULL));
		func = new Func(context, "stop", locals, Type.VOID) {
			@Override
			public Code getVCallCode() {
				Code code = new Code(context);
				code.add(BC.STOPTRIGGER);
				return code;
			}

			@Override
			public boolean isDefined() {
				return true;
			}
		};
		func.code = new Code(context);
		put(func);

	}
}
