package kvv.controllers.register;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SourceDescr implements Serializable {
	public String filename;
	public RegisterDescr[] registers;

	public SourceDescr() {
	}

	public SourceDescr(String filename, RegisterDescr[] registers) {
		this.filename = filename;
		this.registers = registers;
	}
}
