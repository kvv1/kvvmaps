#define ERR_OK 0
#define ERR_UNKNOWN_CMD 1
#define ERR_INVALID_PORT_NUM 2

#define CMD_SETREG 1
#define CMD_GETREG 2
#define CMD_GETREGS 3

#define REG_RELAY0 0
#define REG_RELAYS 8

#define REG_TEMP 9
#define REG_TEMP_PREF 10
#define REG_TEMP_PREF_ON 11
#define REG_TEMP_PREF_2 12

#define REG_ADC0 16


void initCommands();
void handleCmd(char* cmd, char cmdlen);

#define TEMP_INVALID (-9999)
int getTemp10();
                
void startStopTempReg();

extern eeprom int temp_pref_ee;
extern eeprom int temp_pref_ee_2;
extern eeprom char temp_pref_on_ee;

