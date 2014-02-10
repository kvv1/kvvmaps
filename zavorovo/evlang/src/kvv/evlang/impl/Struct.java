package kvv.evlang.impl;

import java.util.ArrayList;
import java.util.List;

import kvv.evlang.ParseException;

public class Struct {

	public Integer index;
	public final Type type;
	public List<NameAndType> fields;
	private final Context context;

	public Funcs funcs;

	public Struct superClass;
	public boolean closed;

	public Struct(Context context, Type type) {
		this.context = context;
		this.type = type;
	}

	public void create(Struct superClass) throws ParseException {
		this.superClass = superClass;
		fields = new ArrayList<NameAndType>();
		funcs = new Funcs(context, false);

		if (type.name.equals("Timer")) {
			addField(Type.INT, "__cnt__");
			funcs.createTimerFuncs();
		}

		if (type.name.equals("Trigger")) {
			addField(Type.INT, "__val__");
			funcs.createTriggerFuncs();
		}

		if (superClass != null) {
			if (!superClass.closed)
				context.throwShouldBeDefined(superClass.type.name);
			fields.addAll(superClass.fields);
			funcs.addAll(superClass.funcs, type);
		}
	}

	public boolean isCreated() {
		return fields != null;
	}

	public Integer getField(String name) {
		for (int i = 0; i < fields.size(); i++)
			if (fields.get(i).name.equals(name))
				return i;
		return null;
	}

	public void addField(Type fieldType, String fieldName)
			throws ParseException {
		if (getField(fieldName) != null)
			context.throwAlreadyDefined(fieldName);
		fields.add(new NameAndType(fieldName, fieldType));
	}

	public int getMask() {
		int mask = 0;
		for (int i = fields.size() - 1; i >= 0; i--) {
			NameAndType nat = fields.get(i);
			mask <<= 1;
			if (nat.type.isRef())
				mask |= 1;
		}
		return mask;
	}
	
//	int[][] n = new int[4][]; 

	public void print() {
		System.out.println(index + " " + type.name + " "
				+ (isCreated() ? fields.size() : "NOT_CREATED"));
		if (isCreated()) {
			System.out.println("\tFunctions:");
			for (Func func : funcs.funcs.values())
				func.print();
		}
	}

	public boolean isAbstract() {
		return index == null;
	}

}
