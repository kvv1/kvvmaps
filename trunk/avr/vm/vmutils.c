#include "interpreter.h"
#include "vmstatus.h"

static int8_t state;

void setState(int s) {
	state = s;
}

int8_t vmGetStatus() {
	return state;
}

int16_t _getReg(uint8_t reg) {
	int16_t val;
	char res = vmGetReg(reg, &val);
	if (!res)
		setState(VMSTATUS_INVALID_REGISTER);
	return val;
}

void _setReg(uint8_t reg, int16_t val) {
	char res = vmSetReg(reg, val);
	if (!res)
		setState(VMSTATUS_INVALID_REGISTER);
}

int16_t stack[STACK_SIZE];
int16_t* stackPtr = stack + STACK_SIZE;


int16_t vmPop() {
	if (stackPtr == stack + STACK_SIZE) {
		setState(VMSTATUS_STACK_UNDERFLOW);
		return 0;
	}
	return *stackPtr++;
}

void vmPush(int16_t v) {
	if (stackPtr == stack) {
		setState(VMSTATUS_STACK_OVERFLOW);
		return;
	}
	*(--stackPtr) = v;
}

