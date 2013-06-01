package kvv.controllers.register;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SourceDescr implements Serializable {
	public String filename;
	public RegisterUI[] registers;

	public SourceDescr() {
	}

	public SourceDescr(String filename, RegisterUI[] registers) {
		this.filename = filename;
		this.registers = registers;
	}
}
