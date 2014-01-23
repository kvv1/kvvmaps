package kvv.evlang.impl;

import java.util.ArrayList;
import java.util.List;

public class Struct {
	public final int idx;
	public final Type type;
	public final boolean isTimer;
	public int timerFunc;
	public final List<NameAndType> fields = new ArrayList<NameAndType>();

	public Struct(int idx, Type type, boolean isTimer) {
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

	public void addField(String fieldName, Type fieldType) {
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
