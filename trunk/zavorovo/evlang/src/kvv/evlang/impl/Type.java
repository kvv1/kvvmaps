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
		if(!equals(INT))
			context.throwExc("should be int type");
	}

	public static void checkComparable(Context context, Type type1, Type type2) throws ParseException {
		if(type1.equals(VOID) || type2.equals(VOID))
			context.throwExc("incompatible types");
		if(type1.equals(type2))
			return;
		if(type1.equals(INT) ^ type2.equals(INT))
			context.throwExc("incompatible types");
		if(!type1.equals(NULL) && !type2.equals(NULL))
			context.throwExc("incompatible types");
	}

	public void checkAssignableTo(Context context, Type type2) throws ParseException {
		checkComparable(context, this, type2);
	}
}
