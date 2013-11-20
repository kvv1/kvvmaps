package kvv.controllers.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ScriptData implements Serializable {
	public String text;
	public boolean enabled;
	public String err;

	public ScriptData() {
	}

	public ScriptData(String text, boolean enabled, String err) {
		this.text = text;
		this.enabled = enabled;
		this.err = err;
	}
}
