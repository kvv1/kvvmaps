#include "interpreter.h"
#include "myio.h"
#include "commands.h"
#include "vm.h"
#include "vmstatus.h"
#include "common.h"
#include <stdint.h>
#include <avr/io.h>
#include <util/delay.h>
#include <avr/eeprom.h>
EEMEM uint8_t code[VMCODE_SIZE];

ee_8(vmstatus);

void initVM() {
	if (getvmstatus() == 255)
		setvmstatus(VMSTATUS_STOPPED);

	if (getvmstatus() == VMSTATUS_RUNNING)
		startVM(VMSTATUS_RUNNING);
	else if(getvmstatus() == VMSTATUS_STOPPED)
		startVM(VMSTATUS_STOPPED);

//	setReg(REG_RAM0, &ee_vmstatus);
}

void startVM(int status) {
	int i;


	if (status == VMSTATUS_RUNNING && getvmstatus() != VMSTATUS_PAUSED) {
		for (i = REG_RAM0; i < REG_RAM0 + REG_RAM_CNT; i++)
			setReg(i, 0);
	}
	vmStart(status);
}

int16_t vmGetReg(uint8_t reg) {
	int16_t val;
	char res = getReg(reg, &val);
	if (!res)
		vmSetStatus(VMSTATUS_INVALID_REGISTER);
	return val;
}

void vmSetReg(uint8_t reg, int16_t val) {
	char res = setReg(reg, val);
	if (!res)
		vmSetStatus(VMSTATUS_INVALID_REGISTER);
}

uint8_t vmReadByte(uint16_t addr) {
	return eeprom_read_byte(code + addr);
}

void vmPrintInt(int16_t n) {
}

int vmGetStatus() {
	return getvmstatus();
}

void vmSetStatus(int status) {
	setvmstatus(status);
}

