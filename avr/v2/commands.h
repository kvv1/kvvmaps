#ifndef COMMANDS_H_
#define COMMANDS_H_

#include <bl.h>

#define ERR_UNKNOWN_CMD 1
#define ERR_INVALID_PORT_NUM 2
#define ERR_WRONG_CMD_FORMAT 3
#define ERR_WRONG_CS 100

#define CMD_MODBUS_GETREGS 3
#define CMD_MODBUS_SETREGS 16
#define CMD_MODBUS_BOOTLOADER 100

uint8_t handleStdCmd(PDU* pdu, uint8_t cmdlen);

#endif /* COMMANDS_H_ */
