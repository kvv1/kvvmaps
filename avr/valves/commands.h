#include "ee.h"

//#define ERR_OK 0
#define ERR_UNKNOWN_CMD 1
#define ERR_INVALID_PORT_NUM 2
#define ERR_WRONG_CMD_FORMAT 3

#define CMD_MODBUS_GETREGS 3
#define CMD_MODBUS_SETREGS 16
#define CMD_GETALLREGS 103 // cmd -> ERR_OK (reg valHi valLo)*
#define CMD_UPLOAD 104 // cmd addrHi addrLo (byte)*
#define CMD_UPLOAD_END 105
#define CMD_GETUI 106 //
#define CMD_VMINIT 107 //

#define REG_RELAY0 0
#define REG_RELAY1 1
#define REG_RELAY2 2
#define REG_RELAY3 3
#define REG_RELAY4 4
#define REG_RELAY5 5
#define REG_RELAY6 6
#define REG_RELAY7 7
#define REG_RELAY_CNT 4

#define REG_RELAYS 8

#define REG_TEMP 9
#define REG_TEMP2 10

#define REG_VMONOFF 13
#define REG_VMSTATE 14
#define REG_INPUTS 15

#define REG_ADC0 16
#define REG_ADC1 17
#define REG_ADC2 18
#define REG_ADC3 19
#define REG_ADC_CNT 4

#define REG_IN0 24
#define REG_IN1 25
#define REG_IN2 26
#define REG_IN3 27
#define REG_IN4 28
#define REG_IN5 29
#define REG_IN6 30
#define REG_IN7 31
#define REG_IN_CNT 4

#define REG_PWM0 32
#define REG_PWM1 33
#define REG_PWM2 34
#define REG_PWM3 35
#define REG_PWM4 36
#define REG_PWM5 37
#define REG_PWM6 38
#define REG_PWM7 39

#define REG_EEPROM0 48
#define REG_EEPROM1 49
#define REG_EEPROM2 50
#define REG_EEPROM3 51
#define REG_EEPROM4 52
#define REG_EEPROM5 53
#define REG_EEPROM6 54
#define REG_EEPROM7 55
#define REG_EEPROM_CNT 8

#define REG_RAM0 56
#define REG_RAM1 57
#define REG_RAM2 58
#define REG_RAM3 59
#define REG_RAM4 60
#define REG_RAM5 61
#define REG_RAM6 62
#define REG_RAM7 63
#define REG_RAM_CNT 8

#define REG_INPULLUP0 248
#define REG_INPULLUP1 249
#define REG_INPULLUP2 250
#define REG_INPULLUP3 251
#define REG_INPULLUP4 252
#define REG_INPULLUP5 253
#define REG_INPULLUP6 254
#define REG_INPULLUP7 255

//void initCommands();

char getReg(uint8_t reg, int* val);
char setReg(uint8_t reg, int val);

int checkCode();

void initCommands();

ee_16_decl(CodeLen);
ee_16_decl(CodeCRC);

void handlePWM(int ms);
