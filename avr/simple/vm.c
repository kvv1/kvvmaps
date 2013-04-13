#include "interpreter.h"
#include "myio.h"
#include <stdint.h>
#include <avr/io.h>
#include <util/delay.h>
#include <avr/eeprom.h>
EEMEM uint8_t code[] = { 0x00, 0x54, 0x03, 0x00, 0x0D, 0x00, 0x11, 0x00, 0x14,
		0x00, 0x18, 0x80, 0x1B, 0x00, 0x1D, 0x01, 0x00, 0x20, 0x02, 0x00, 0x34,
		0x00, 0x38, 0x1B, 0x00, 0x18, 0x1B, 0x01, 0x59, 0xF8, 0x02, 0x00, 0x64,
		0x05, 0x00, 0x00, 0xB8, 0x54, 0x14, 0x01, 0xB8, 0x18, 0x00, 0xB8, 0x54,
		0x13, 0x01, 0xB8, 0x18, 0x00, 0xB8, 0x01, 0xB8, 0x18, 0x00, 0xB8, 0x11,
		0xF8, 0xB8, 0x40, 0x14, 0x08, 0x04, 0x40, 0xC2, 0x09, 0x02, 0x41, 0xC2,
		0x02, 0x00, 0x64, 0x05, 0x00, 0x00, 0x4A, 0x54, 0x0A, 0x01, 0x41, 0x18,
		0x42, 0x18, 0x43, 0x18, 0x00, 0x78, };

static int16_t regs[256];

int16_t vmGetReg(uint8_t reg) {
	return regs[reg];
}

void vmSetReg(uint8_t reg, int16_t val) {
	regs[reg] = val;
}

uint8_t vmReadByte(uint16_t addr) {
	return eeprom_read_byte(code + addr);
}

void vmPrintInt(int16_t n) {
	print1("%d ", n);
}

static int status;

int vmGetStatus() {
	return status;
}

void vmSetStatus(int st) {
	status = st;
}

#define VM_STEP 10

int vmMain(void) {
	vmStart(1);
//	vmInit();

	for (;;) {
		_delay_ms(VM_STEP);
		vmStep(VM_STEP);
	}

	return 0;
}
