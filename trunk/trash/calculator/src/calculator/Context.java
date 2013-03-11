package calculator;

import java.util.LinkedList;

public class Context {
	private static class VariableItem {
		public final String name;
		public final int value;

		public VariableItem(String name, int value) {
			this.name = name;
			this.value = value;
		}
	}

	private LinkedList<Context.VariableItem> variables = new LinkedList<Context.VariableItem>();

	public void putVariable(String name, int value) {
		variables.addFirst(new VariableItem(name, value));
	}

	public void popVariable() {
		variables.removeFirst();
	}

	public Integer getVariable(String name) {
		for (Context.VariableItem item : variables)
			if (item.name.equals(name))
				return item.value;
		return null;
	}
}