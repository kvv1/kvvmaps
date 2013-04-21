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

int main(void) {
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
