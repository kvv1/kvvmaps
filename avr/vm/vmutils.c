#include "interpreter.h"
#include "vmstatus.h"

static int8_t state;

void onVMStatusChanged();

void vmSetStatus(int8_t s) {
	state = s;
	onVMStatusChanged();
}

int8_t vmGetStatus() {
	return state;
}

int16_t _getReg(uint8_t reg) {
	int16_t val;
	char res = vmGetReg(reg, &val);
	if (!res)
		vmSetStatus(VMSTATUS_INVALID_REGISTER);
	return val;
}

void _setReg(uint8_t reg, int16_t val) {
	char res = vmSetReg(reg, val);
	if (!res)
		vmSetStatus(VMSTATUS_INVALID_REGISTER);
}

int16_t stack[STACK_SIZE];
int16_t* stackPtr = stack + STACK_SIZE;

static int8_t checkStack(int16_t* ptr) {
	if (ptr > stack + STACK_SIZE) {
		vmSetStatus(VMSTATUS_STACK_UNDERFLOW);
		return 0;
	}
	if (stackPtr < stack) {
		vmSetStatus(VMSTATUS_STACK_OVERFLOW);
		return 0;
	}
	return 1;
}

void vmSetStack(int16_t* ptr) {
	if (checkStack(ptr))
		stackPtr = ptr;
}

void vmChangeStack(int16_t n) {
	if (checkStack(stackPtr - n))
		stackPtr -= n;
}

int16_t vmPop() {
	if (!checkStack(stackPtr + 1))
		return 0;
	return *stackPtr++;
}

void vmPush(int16_t v) {
	if (checkStack(stackPtr - 1))
		*(--stackPtr) = v;
}

