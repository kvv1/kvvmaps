package kvv.controllers.shared;

public class Constants {
	public static final int REG_CONNECTION = -1;
	
	public static final int REG_RELAY0 = 0;
	public static final int REG_RELAYS = 8;
	public static final int REG_TEMP = 9;
	public static final int REG_TEMP_PREF = 10;
	public static final int REG_TEMP_PREF_ON = 11;
	public static final int REG_TEMP_PREF_2 = 12;
	public static final int REG_ADC0 = 16;
	
	public static final String ROOT = "c:/zavorovo/";
	public static final String scheduleFile = ROOT + "schedule.txt";
	public static final String commandsFile = ROOT + "commands.txt";
	public static final String controllersFile = ROOT + "controllers.txt";
	public static final String objectsFile = ROOT + "objects.txt";
	public static final String propsFile = ROOT + "server.properties";
	public static final String logFile = ROOT + "log.txt";
}
