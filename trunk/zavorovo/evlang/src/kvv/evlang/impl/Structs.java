package kvv.evlang.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import kvv.evlang.ParseException;

public class Structs {
	private static final String TIMER_FUNC_NAME = "__timer_func__";
	private static final String TIMER_COUNTER_NAME = "__timer_counter__";

	public Structs(Context context) {
		this.context = context;
	}

	private final Context context;
	private Map<String, Struct> structs = new LinkedHashMap<String, Struct>();

	public Struct createStruct(String name, boolean isTimer)
			throws ParseException {
		if(structs.containsKey(name))
			context.throwExc(name + " already defined");
		Struct str = new Struct(context, structs.size(), new Type(name),
				isTimer);
		structs.put(name, str);
		return str;
	}

	public Struct createXTimer(String name) throws ParseException {
		Struct struct = createStruct(name, true);
		struct.addField(Type.INT, TIMER_FUNC_NAME);
		struct.addField(Type.INT, TIMER_COUNTER_NAME);
		return struct;
	}

	public int getFieldIndex(Type type, String fieldName) throws ParseException {
		if (!type.isRef())
			context.throwExc("should be struct type");
		Struct struct = structs.get(type.name);
		Integer idx = struct.getField(fieldName);
		if (idx == null)
			context.throwExc(fieldName + " - ?");
		return idx;
	}

	public Struct get(String name) {
		return structs.get(name);
	}

	public Collection<Struct> values() {
		return structs.values();
	}

	public int size() {
		return structs.size();
	}

}
