package kvv.evlang.impl;

import java.util.ArrayList;
import java.util.List;

import kvv.evlang.ParseException;

public class Struct {
	public final int idx;
	public final Type type;
	public final boolean isTimer;
	public int timerFunc;
	public final List<NameAndType> fields = new ArrayList<NameAndType>();
	private final Context context;

	public Struct(Context context, int idx, Type type, boolean isTimer) {
		this.context = context;
		this.idx = idx;
		this.type = type;
		this.isTimer = isTimer;
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
			context.throwExc(fieldName + " already defined");
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
}
