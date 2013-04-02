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
	public static final int REG_ADC1 = 17;
	public static final int REG_ADC2 = 18;
	public static final int REG_ADC3 = 19;

	public static final int REG_IN0 = 24;
	public static final int REG_IN1 = 25;
	public static final int REG_IN2 = 26;
	public static final int REG_IN3 = 27;
	public static final int REG_IN4 = 28;
	public static final int REG_IN5 = 29;
	public static final int REG_IN6 = 30;
	public static final int REG_IN7 = 31;

	public static final int REG_EEPROM0 = 48;
	public static final int REG_EEPROM1 = 49;
	public static final int REG_EEPROM2 = 50;
	public static final int REG_EEPROM3 = 51;
	public static final int REG_EEPROM4 = 52;
	public static final int REG_EEPROM5 = 53;
	public static final int REG_EEPROM6 = 54;
	public static final int REG_EEPROM7 = 55;

	public static final int REG_RAM0 = 56;
	public static final int REG_RAM1 = 57;
	public static final int REG_RAM2 = 58;
	public static final int REG_RAM3 = 59;
	public static final int REG_RAM4 = 60;
	public static final int REG_RAM5 = 61;
	public static final int REG_RAM6 = 62;
	public static final int REG_RAM7 = 63;

	public static final String ROOT = "c:/zavorovo/";
	public static final String scheduleFile = ROOT + "schedule.txt";
	public static final String commandsFile = ROOT + "commands.txt";
	public static final String controllersFile = ROOT + "controllers.txt";
	public static final String objectsFile = ROOT + "objects.txt";
	public static final String propsFile = ROOT + "server.properties";
	public static final String logFile = ROOT + "log.txt";
}
