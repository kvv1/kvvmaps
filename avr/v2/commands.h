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
#define CMD_GETALLREGS 103 // cmd -> ERR_OK (reg valHi valLo)*
#define CMD_UPLOAD 104 // cmd addrHi addrLo (byte)*
#define CMD_UPLOAD_END 105
#define CMD_GETUI 106 //
#define CMD_VMINIT 107 //
#define CMD_GETRULES 108 // -> N rules
#define CMD_SETRULE 109 // N rule ->

uint8_t handleStdCmd(PDU* pdu, uint8_t cmdlen);

#endif /* COMMANDS_H_ */
