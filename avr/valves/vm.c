#include "interpreter.h"
#include "myio.h"
#include "commands.h"
#include "vm.h"
#include "vmstatus.h"
#include "common.h"
#include <stdint.h>
#include <avr/io.h>
#include <util/delay.h>
EEMEM uint8_t code[VMCODE_SIZE];

ee_8(vmonoff);

void initVM() {
	if (getvmonoff() == 255)
		setvmonoff(0);
	startVM(getvmonoff());
}

void startVM(int8_t start) {
	if (start) {
		int i;
		for (i = REG_RAM0; i < REG_RAM0 + REG_RAM_CNT; i++)
			setReg(i, 0);
	}

	if (start && !checkCode()) {
		if(getCodeLen() == 0)
			vmSetStatus(VMSTATUS_STOPPED);
		else
			vmSetStatus(VMSTATUS_WRONG_CHECKSUM);
		return;
	}

	vmStart(start);
}

int8_t vmGetReg(uint8_t reg, int16_t* val) {
	return getReg(reg, val);
}

int8_t vmSetReg(uint8_t reg, int16_t val) {
	return setReg(reg, val);
}

uint8_t vmReadByte(uint16_t addr) {
	return EEPROM_read((uint16_t) code + addr);
}

void vmPrintInt(int16_t n) {
}

