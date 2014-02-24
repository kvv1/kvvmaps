#ifndef UTILS_H_
#define UTILS_H_

#include <string.h>
#include "inc/bl.h"

#define MAGIC8 0x5A
#define MAGIC16 0xE6C9

#define MAGIC_APP_0 0x1234
#define MAGIC_APP_2 0xABCD
#define MAGIC_APP_DWORD 0x1234ABCDUL

#define MODBUS_BOOTLOADER 100
#define MODBUS_CLEAR_APP 101
#define MODBUS_UPLOAD_APP 102
#define MODBUS_ENABLE_APP 103

#define MODBUS_HELLO 120

#define START_TIMEOUT_US 5000000UL

#define BOOT_INPUT_BUFFER_SIZE (512 + 10)

typedef struct {
	volatile uint16_t magic16;
	uint16_t lastPage;
	uint16_t inputIdx;
	uint8_t inputBuffer[BOOT_INPUT_BUFFER_SIZE];
} Globals;

extern Globals globals;


void packetReceived(uint8_t* buffer, int len); // returns consumed flag

uint8_t isAppOK();

#endif /* UTILS_H_ */
