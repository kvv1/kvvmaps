package kvv.heliostat.server.envir.controller;

public class Command {
	public static final byte CMD_MODBUS_SETREGS = 16;
	public static final byte CMD_MODBUS_GETREGS = 3;

	public static final byte MODBUS_BOOTLOADER = 100;
	public static final byte MODBUS_HELLO = 90;
	public static final byte MODBUS_UPLOAD_APP = 91;
	public static final byte MODBUS_ENABLE_APP = 92;

	public static final byte CMD_GETALLREGS = 103;
	public static final byte MODBUS_GETRULES = 108;
	public static final byte MODBUS_SETRULE = 109;
}
