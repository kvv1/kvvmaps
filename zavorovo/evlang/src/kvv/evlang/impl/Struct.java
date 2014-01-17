package kvv.evlang.impl;

import java.util.LinkedHashMap;
import java.util.Map;

public class Struct {
	public final String name;
	public final Map<String, Type> fields = new LinkedHashMap<String, Type>();

	public Struct(String name) {
		this.name = name;
	}
}
