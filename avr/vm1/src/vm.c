#include "interpreter.h"
#include "vmstatus.h"

#include <stdio.h>
#include <stdint.h>

static uint8_t code[512];
static int16_t regs[256];

int8_t vmGetReg(uint8_t reg, int16_t* val) {
	*val = regs[reg];
	return 1;
}

int8_t vmSetReg(uint8_t reg, int16_t val) {
	regs[reg] = val;
	return 1;
}

uint8_t vmReadByte(uint16_t addr) {
	return code[addr];
}

void vmPrintInt(int16_t n) {
	printf("%d ", n);
	fflush(stdout);
}

#define VM_STEP 10

#define CRC16_INIT 0xffff

uint16_t crc16_step(uint8_t c, uint16_t crc_val) {
	crc_val ^= (uint16_t) c;

	char j = 8;
	while (j--) {
		uint8_t carry = crc_val & 0x0001;
		crc_val >>= 1;
		if (carry)
			crc_val ^= 0xa001;
	}

	return crc_val;
}

int main(void) {
	uint8_t data[] = { 0x10, 0x05, 0x00, 0x02, 0xFF, 0x00 };

	//printf("%04X\n", crc16(data, sizeof(data)));

	return 0;
}

int _main(void) {
	FILE* file = fopen("d:/vm/code.bin", "rb");

	fread(code, sizeof(code), 1, file);
	fclose(file);

	vmStart(1);
//	vmInit();

	for (;;) {
		int status;
		status = vmGetStatus();
		if (status != VMSTATUS_RUNNING) {
			printf("status = %d\n", status);
			return 1;
		}

		_sleep(VM_STEP);
		vmStep(VM_STEP);
	}

	return 0;
}
