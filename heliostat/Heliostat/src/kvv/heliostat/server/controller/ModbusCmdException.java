package kvv.heliostat.server.controller;

import java.io.IOException;

public class ModbusCmdException extends IOException {
	private static final long serialVersionUID = 1L;

	public int cmd;

	public ModbusCmdException(int cmd) {
		super("modbus error: " + ErrorCode.values()[cmd].name());
		this.cmd = cmd;
	}

}