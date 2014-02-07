package kvv.evlang.impl;

import kvv.evlang.ParseException;

public class Type {
	public static final Type INT = new Type("int");
	public static final Type VOID = new Type("void");
	public static final Type NULL = new Type("null");

	public final String name;

	public Type(String typeName) {
		this.name = typeName;
	}

	public int getSize() {
		if (this.equals(VOID))
			return 0;
		return 1;
	}

	public boolean isRef() {
		return !this.equals(INT) && !this.equals(VOID);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return name.equals(((Type) obj).name);
	}

	public void checkInt(Context context) throws ParseException {
		if (!equals(INT))
			context.throwExc("should be int type");
	}

	public void checkNotVoid(Context context) throws ParseException {
		if (equals(Type.VOID))
			context.throwExc("'void' type not allowed");
	}

	public static void checkComparable(Context context, Type type1, Type type2)
			throws ParseException {
		if (type1.equals(VOID) || type2.equals(VOID))
			context.throwExc("incompatible types: " + type1.name + ","
					+ type2.name);

		if (type1.equals(INT) && type2.equals(INT))
			return;

		if (type1.equals(INT) || type2.equals(INT))
			context.throwExc("incompatible types: " + type1.name + ","
					+ type2.name);

		if (type1.equals(NULL) || type2.equals(NULL))
			return;

		if (type1.isSubclassOf(context, type2)
				|| type2.isSubclassOf(context, type1))
			return;

		context.throwExc("incompatible types: " + type1.name + "," + type2.name);
	}

	public void checkAssignableTo(Context context, Type type2)
			throws ParseException {

		if (equals(VOID))
			context.throwExc("cannot assign " + name + " to " + type2.name);

		if (equals(INT) && type2.equals(INT))
			return;

		if (equals(INT) || type2.equals(INT))
			context.throwExc("cannot assign " + name + " to " + type2.name);

		if (equals(NULL))
			return;

		if (isSubclassOf(context, type2))
			return;

		context.throwExc("cannot assign " + name + " to " + type2.name);
	}

	private boolean isSubclassOf(Context context, Type type)
			throws ParseException {
		if (equals(type))
			return true;
		Struct s = context.structs.get(name);
		if (s.superClass == null)
			return false;
		return s.superClass.type.isSubclassOf(context, type);
	}
}
