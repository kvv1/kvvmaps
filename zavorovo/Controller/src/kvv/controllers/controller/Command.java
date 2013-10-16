package kvv.controllers.controller;

public class Command {
	public static final byte CMD_MODBUS_SETREGS = 16;
	public static final byte CMD_MODBUS_GETREGS = 3;
	
	public static final byte CMD_GETALLREGS = 103;
	public static final byte CMD_UPLOAD = 104;
	public static final byte CMD_UPLOAD_END = 105;
	public static final byte CMD_GETUI = 106;
	public static final byte CMD_VMINIT = 107;
}
