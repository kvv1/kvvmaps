package kvv.evlang.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import kvv.evlang.ParseException;

public class Structs {
	public Structs(Context context) {
		this.context = context;
	}

	private final Context context;
	private Map<String, Struct> structs = new LinkedHashMap<String, Struct>();
	
	int nextIndex;

	public Struct createStruct(String name) throws ParseException {
		Struct str = structs.get(name);
		if (str == null) {
			str = new Struct(context, new Type(name));
			structs.put(name, str);
		} else if (str.fields != null) {
			context.throwAlreadyDefined(name);
		}

		return str;
	}

	public void create(String name, String superName) throws ParseException {
		Struct str = structs.get(name);
		Struct superStruct = null;
		if (superName != null) {
			superStruct = get(superName);
			if (superStruct == null)
				context.throwWatIsIt(superName);
		}
		str.create(superStruct);
	}

	public int getFieldIndex(Type type, String fieldName) throws ParseException {
		type.checkSimpleRef(context);
		Struct struct = structs.get(type.name);
		Integer idx = struct.getField(fieldName);
		if (idx == null)
			context.throwWatIsIt(fieldName);
		return idx;
	}

	public Struct get(String name) throws ParseException {
		Struct res = structs.get(name);
		if (res == null)
			context.throwWatIsIt(name);
		return res;
	}

	public Collection<Struct> values() {
		return structs.values();
	}

	public int size() {
		return structs.size();
	}

	public void close(String name) throws ParseException {
		get(name).closed = true;
	}

	public int getStructIndex(Struct str) {
		if(str.isAbstract())
			str.index = nextIndex++;
		return str.index;
	}

}
