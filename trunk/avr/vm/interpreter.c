#include "interpreter.h"
#include "vmstatus.h"

#include <stdint.h>
#include <string.h>

enum {
	RET,
	RET_N,
	RETI,
	RETI_N,
	LIT,
	SETREG,
	GETREG,
	SETTIMER_S,
	SETTIMER_MS,
	STOPTIMER,
	QBRANCH,
	BRANCH,
	ADD,
	SUB,
	MUL,
	DIV,
	NEGATE,
	OR,
	AND,
	NOT,
	LT,
	LE,
	GT,
	GE,
	EQ,
	NEQ,
	DROP,
	INC,
	DEC,
	CALL,
	RESERVED,
	MULDIV,
	GETLOCAL,
	SETLOCAL,
	ENTER,
	PRINT,
	SETEXTREG,
	GETEXTREG,
	THROW,
};

#define GETREGSHORT 0x80
#define SETREGSHORT 0xC0
#define LITSHORT 0x40

static uint16_t codeOffset;
static uint16_t events;
static uint16_t timers;
static uint16_t funcs;
static uint16_t TCBs;
static int ntimers;
static int nevents;
static int nfuncs;
static int nTCB;

#define MAX_TIMERS 16
#define MAX_EVENTS 16

static int32_t vmTimerCnts[MAX_TIMERS];
static int16_t vmEventStates[MAX_EVENTS];

static uint16_t fp;
static uint16_t ip;

static int16_t vmExec(uint16_t ip);

static uint16_t getUint16(uint16_t addr) {
	return (vmReadByte(addr) << 8) | vmReadByte(addr + 1);
}

static uint16_t vmGetTimerHandler(uint8_t timer) {
	return getUint16(timers + timer * 2);
}

static uint16_t vmGetEventCondition(uint8_t event) {
	return getUint16(events + event * 4);
}

static uint16_t vmGetEventHandler(uint8_t event) {
	return getUint16(events + event * 4 + 2);
}

static uint16_t vmGetFuncCode(uint8_t func) {
	return getUint16(funcs + func * 2);
}

int getUIStart() {
	return 0;
}

int getUIEnd() {
	uint16_t ptr = 0;
	int nUI = vmReadByte(ptr);
	ptr++;
	while (nUI--) {
		ptr++; // reg
		ptr++; // type
		ptr += vmReadByte(ptr) + 1; // text
	}
	return ptr;
}

static void initVars() {
	uint16_t ptr = getUIEnd();
	nevents = vmReadByte(ptr);
	ptr++;
	events = ptr;
	ptr += nevents * 4;

	ntimers = vmReadByte(ptr);
	ptr++;
	timers = ptr;
	ptr += ntimers * 2;

	nfuncs = vmReadByte(ptr);
	ptr++;
	funcs = ptr;
	ptr += nfuncs * 2;

	nTCB = vmReadByte(ptr);
	ptr++;
	TCBs = ptr;
	ptr += nTCB * 6;

	codeOffset = ptr;

	stackPtr = stack + STACK_SIZE;

	memset(vmTimerCnts, 0, sizeof(vmTimerCnts));
	memset(vmEventStates, 0, sizeof(vmEventStates));
}

void vmInit() {
	if (vmGetStatus() != VMSTATUS_STOPPED)
		return;
	initVars();
	vmSetStatus(VMSTATUS_RUNNING);
	vmExec(vmGetFuncCode(0) + codeOffset);
	vmSetStatus(VMSTATUS_STOPPED);
}

static void vmMain() {
	initVars();
	vmSetStatus(VMSTATUS_RUNNING);
	vmExec(vmGetFuncCode(1) + codeOffset);
}

static int16_t eval(uint16_t ip) {
	if (vmExec(ip) == 0)
		return vmPop();
	return 0;
}

void vmStep(int ms) {
	int i;

	if (vmGetStatus() != VMSTATUS_RUNNING)
		return;
	for (i = 0; i < nevents; i++) {
		uint16_t cond = vmGetEventCondition(i);
		int change = (cond & 0x8000) != 0;
		cond &= 0x7FFF;
		int16_t newState = eval(cond + codeOffset);
		if (vmGetStatus() != VMSTATUS_RUNNING)
			return;
		if ((!change && !vmEventStates[i] && newState)
				|| (change && vmEventStates[i] != newState)) {
			vmExec(vmGetEventHandler(i) + codeOffset);
			if (vmGetStatus() != VMSTATUS_RUNNING)
				return;
		}
		vmEventStates[i] = newState;
	}

	for (i = 0; i < ntimers; i++) {
		if (vmTimerCnts[i]) {
			vmTimerCnts[i] -= ms;
			if (vmTimerCnts[i] <= 0) {
				vmTimerCnts[i] = 0;
				vmExec(vmGetTimerHandler(i) + codeOffset);
				if (vmGetStatus() != VMSTATUS_RUNNING)
					return;
			}
		}
	}
}

static uint16_t findCatchBlock(uint16_t ip) {
	uint16_t bestHandler = 0;
	uint16_t bestFrom = 0;
	int i;

	for (i = 0; i < nTCB; i++) {
		uint16_t tcb = TCBs + i * 6;
		uint16_t from = getUint16(tcb);
		uint16_t to = getUint16(tcb + 2);
		if (ip >= from && ip < to && from > bestFrom) {
			bestFrom = from;
			bestHandler = getUint16(tcb + 4);
		}
	}

	return bestHandler;
}

static void call(uint16_t addr) {
	vmPush(ip);
	vmPush(fp);
	fp = stackPtr - stack;
	ip = addr;
}

static void ret() {
	vmSetStack(stack + fp);
	fp = vmPop();
	ip = vmPop();
}

static void throwException(int16_t e) {
	for (;;) {
		vmSetStack(stack + fp);
		uint16_t catchBlock = findCatchBlock(ip - codeOffset);
		if (catchBlock != 0) {
			vmPush(e);
			ip = catchBlock + codeOffset;
			break;
		} else {
			ret();
			if (ip == 0)
				return;
		}
	}
}

#define LOCAL(n) (stack[fp + 2 + (n)])

static int16_t vmExec(uint16_t _ip) {

	stackPtr = stack + STACK_SIZE;
	ip = 0;
	fp = 0;

	call(_ip);

	for (;;) {
		if (vmGetStatus() != VMSTATUS_RUNNING)
			return -1;

		uint8_t c = vmReadByte(ip++);

		if ((c & 0xC0) == (GETREGSHORT & 0xFF)) {
			vmPush(_getReg(c & 0x3F));
			continue;
		} else if ((c & 0xC0) == (SETREGSHORT & 0xFF)) {
			_setReg(c & 0x3F, vmPop());
			continue;
		} else if ((c & 0xC0) == (LITSHORT & 0xFF)) {
			int16_t v = (int16_t) (((int8_t) ((c & 0x3F) << 2)) >> 2);
			vmPush(v);
			continue;
		}

		switch (c) {
		case CALL: {
			uint16_t addr = vmGetFuncCode(vmReadByte(ip++)) + codeOffset;
			call(addr);
			break;
		}
		case RET:
			ret();
			if (ip == 0)
				return 0;
			break;
		case RET_N: {
			uint8_t n = vmReadByte(ip);
			ret();
			vmChangeStack(-n);
			if (ip == 0)
				return 0;
			break;
		}
		case RETI: {
			int16_t res = vmPop();
			ret();
			vmPush(res);
			if (ip == 0)
				return 0;
			break;
		}
		case RETI_N: {
			uint8_t n = vmReadByte(ip);
			int16_t res = vmPop();
			ret();
			vmChangeStack(-n);
			vmPush(res);
			if (ip == 0)
				return 0;
			break;
		}
		case THROW: {
			int16_t res = vmPop();
			throwException(res);
			if (ip == 0)
				return res;
			break;
		}
		case GETLOCAL: {
			int n = (int8_t) vmReadByte(ip++);
			vmPush(LOCAL(n));
			break;
		}
		case SETLOCAL: {
			int n = (int8_t) vmReadByte(ip++);
			LOCAL(n) = vmPop();
			break;
		}
		case ENTER: {
			uint16_t link = vmPop();
			uint16_t ret = vmPop();
			int n = vmReadByte(ip++);
			vmChangeStack(n);
			vmPush(ret);
			vmPush(link);
			fp = stackPtr - stack;
			break;
		}
		case LIT:
			vmPush(getUint16(ip));
			ip += 2;
			break;

		case SETREG:
			_setReg(vmReadByte(ip++), vmPop());
			break;
		case GETREG:
			vmPush(_getReg(vmReadByte(ip++)));
			break;
		case SETTIMER_S:
			vmTimerCnts[vmReadByte(ip++)] = (int32_t) vmPop() * 1000;
			break;
		case SETTIMER_MS:
			vmTimerCnts[vmReadByte(ip++)] = vmPop();
			break;
		case STOPTIMER:
			vmTimerCnts[vmReadByte(ip++)] = 0;
			break;
		case QBRANCH:
			if (vmPop())
				ip++;
			else
				ip += (int8_t) vmReadByte(ip) + 1;
			break;
		case BRANCH:
			ip += (int8_t) vmReadByte(ip) + 1;
			break;
		case ADD:
			vmPush(vmPop() + vmPop());
			break;
		case SUB: {
			int16_t r = vmPop();
			vmPush(vmPop() - r);
			break;
		}
		case MUL:
			vmPush(vmPop() * vmPop());
			break;
		case DIV: {
			int16_t r = vmPop();
			if (r == 0) {
				throwException(ARITHMETIC_EXCEPTION);
				if (ip == 0)
					return ARITHMETIC_EXCEPTION;
			} else {
				vmPush(vmPop() / r);
			}
			break;
		}
		case OR: {
			int n1 = vmPop();
			int n2 = vmPop();
			vmPush(n1 || n2);
			break;
		}
		case AND: {
			int n1 = vmPop();
			int n2 = vmPop();
			vmPush(n1 && n2);
			break;
		}
		case NOT:
			vmPush(!vmPop());
			break;
		case NEGATE: {
			vmPush(-vmPop());
			break;
		}
		case LT: {
			int16_t r = vmPop();
			vmPush(vmPop() < r);
			break;
		}
		case LE: {
			int16_t r = vmPop();
			vmPush(vmPop() <= r);
			break;
		}
		case GT: {
			int16_t r = vmPop();
			vmPush(vmPop() > r);
			break;
		}
		case GE: {
			int16_t r = vmPop();
			vmPush(vmPop() >= r);
			break;
		}
		case EQ:
			vmPush(vmPop() == vmPop());
			break;
		case NEQ:
			vmPush(vmPop() != vmPop());
			break;
		case DROP:
			vmPop();
			break;
		case PRINT:
			vmPrintInt(vmPop());
			break;
		case INC: {
			uint8_t reg = vmReadByte(ip++);
			_setReg(reg, _getReg(reg) + 1);
			break;
		}
		case DEC: {
			uint8_t reg = vmReadByte(ip++);
			_setReg(reg, _getReg(reg) - 1);
			break;
		}
		case MULDIV: {
			int32_t n3 = vmPop();
			int32_t n2 = vmPop();
			int32_t n1 = vmPop();
			if (n3 == 0) {
				throwException(ARITHMETIC_EXCEPTION);
				if (ip == 0)
					return ARITHMETIC_EXCEPTION;
			} else {
				vmPush(n1 * n2 / n3);
			}
			break;
		}
		default:
			vmSetStatus(VMSTATUS_INVALID_BYTECODE);
			break;
		}
	}

	return -1;
}

void vmStart(int8_t b) {
	if (b) {
		vmMain();
	} else {
		vmSetStatus(VMSTATUS_STOPPED);
	}
}

