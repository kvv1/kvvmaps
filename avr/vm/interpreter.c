#include "interpreter.h"
#include "vmstatus.h"
#include "heap.h"
#include "context.h"

#include <stdint.h>
#include <string.h>

enum {
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
	MULDIV,
	PRINT,
	SETEXTREG,
	GETEXTREG,
	THROW,
	NEW,
	VCALL,
	TRAP,
	STOPTRIGGER,
	SETTRIGGER,
	NEWOBJARR,
	NEWINTARR,
	SETARRAY,
	GETARRAY,
	ARRAYLENGTH,
};

#define GETREG_SHORT 0x40
#define SETREG_SHORT 0x50
#define GETLOCAL_SHORT  0x60
#define SETLOCAL_SHORT  0x70
#define GETFIELD_SHORT  0x80
#define SETFIELD_SHORT  0x90
#define CALL_SHORT  0xA0
#define RET_SHORT 0xB0
#define RETI_SHORT  0xC0
#define LIT_SHORT  0xD0
#define ENTER_SHORT  0xE0
#define NEW_SHORT  0xF0

static int nRefs;
static int nTCB;

static uint16_t codeOffset;
static uint16_t funcs;
static uint16_t TCBs;
static uint16_t constPool;
static uint16_t regPool;
static uint16_t refs;

int vmGetRefsCnt() {
	return nRefs;
}

int vmGetRefReg(int n) {
	return vmReadByte(refs + n);
}

#define MAX_TYPES 16

static int16_t types[MAX_TYPES];

static uint16_t fp;
static uint16_t ip;
static uint16_t exception;

static uint16_t getUint16(uint16_t addr) {
	return (vmReadByte(addr) << 8) | vmReadByte(addr + 1);
}

uint16_t vmGetFuncCode(uint8_t func) {
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

void initVars() {
	int nfuncs;
	int nConstPool;
	int nRegPool;
	int i;

	uint16_t ptr = getUIEnd();
	nfuncs = vmReadByte(ptr);
	ptr++;
	funcs = ptr;
	ptr += nfuncs * 2;

	nTCB = vmReadByte(ptr);
	ptr++;
	TCBs = ptr;
	ptr += nTCB * 6;

	nConstPool = vmReadByte(ptr);
	ptr++;
	constPool = ptr;
	ptr += nConstPool * 2;

	nRegPool = vmReadByte(ptr);
	ptr++;
	regPool = ptr;
	ptr += nRegPool * 2;

	nRefs = vmReadByte(ptr);
	ptr++;
	refs = ptr;
	ptr += nRefs;

	int nTypes = vmReadByte(ptr);
	ptr++;
	for (i = 0; i < nTypes; i++) {
		types[i] = ptr;
		ptr += 3;
		int vtableCnt = vmReadByte(ptr++);
		ptr += vtableCnt * 2;
	}

	codeOffset = ptr;

	stackPtr = stack + STACK_SIZE;

	//heapAlloc()
}

#define TYPE_SZ_OFF 0
#define TYPE_MASK_OFF 1
#define TYPE_VTABLE_OFF 4

int getTypeSize(int typeIdx) {
	return vmReadByte(types[typeIdx] + TYPE_SZ_OFF);
}

uint16_t getTypeMask(int typeIdx) {
	return getUint16(types[typeIdx] + TYPE_MASK_OFF);
}

uint16_t getVMethod(int typeIdx, int methodIdx) {
	return getUint16(types[typeIdx] + TYPE_VTABLE_OFF + methodIdx * 2);
}

static uint16_t findCatchBlock(uint16_t ip) {
	int i;

	for (i = 0; i < nTCB; i++) {
		uint16_t tcb = TCBs + i * 6;
		uint16_t from = getUint16(tcb);
		uint16_t to = getUint16(tcb + 2);
		if (ip > from && ip <= to)
			return getUint16(tcb + 4);
	}

	return 0;
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
			if (ip == 0) {
				exception = e;
				return;
			}
		}
	}
}

static void _newArr(int sz, int objArr) {
	if (sz < 0) {
		throwException(ARRAYINDEX_EXCEPTION);
		return;
	}
	int a = heapAlloc2(sz, 1, objArr);
	if (a == 0) {
		throwException(OUTOFMEMORY_EXCEPTION);
		return;
	}
	vmPush(a);
}

static void _new(short n) {
	int sz = getTypeSize(n);
	int a = heapAlloc2(n, 0, 0);
	if (a == 0) {
		throwException(OUTOFMEMORY_EXCEPTION);
		return;
	}
	while (sz-- > 0)
		heapSet(a, sz, vmPop());
	vmPush(a);
}

#define LOCAL(n) (stack[fp + 2 + (n)])

int16_t eval(uint16_t ip) {
	if (vmExec(ip) == 0)
		return vmPop();
	return 0;
}

int16_t eval1(uint16_t ip, int16_t param) {
	if (vmExec1(ip, param) == 0)
		return vmPop();
	return 0;
}

static int16_t interpret(uint16_t addr);

int16_t vmExec(uint16_t addr) {
	stackPtr = stack + STACK_SIZE;
	return interpret(addr);
}

int16_t vmExec1(uint16_t addr, int16_t param) {
	stackPtr = stack + STACK_SIZE;
	vmPush(param);
	return interpret(addr);
}

int16_t vmExec2(uint16_t addr, int16_t param1, int16_t param2) {
	stackPtr = stack + STACK_SIZE;
	vmPush(param1);
	vmPush(param2);
	return interpret(addr);
}

int16_t vmExec3(uint16_t addr, int16_t param1, int16_t param2, int16_t param3) {
	stackPtr = stack + STACK_SIZE;
	vmPush(param1);
	vmPush(param2);
	vmPush(param3);
	return interpret(addr);
}

static int16_t interpret(uint16_t addr) {
	addr += codeOffset;

	ip = 0;
	fp = 0;
	exception = 0;

	call(addr);

	while (!exception) {
		if (vmGetStatus() != VMSTATUS_RUNNING)
			return -1;

		uint8_t c = vmReadByte(ip++);

		if ((c & 0xC0) != 0) {
			uint8_t param = (c & 0x0F);
			switch (c & 0xF0) {
			case GETREG_SHORT:
				vmPush(_getReg(getUint16(param + regPool)));
				break;
			case SETREG_SHORT:
				_setReg(getUint16(param + regPool), vmPop());
				break;
			case GETLOCAL_SHORT:
				vmPush(LOCAL(param));
				break;
			case SETLOCAL_SHORT:
				LOCAL(param) = vmPop();
				break;
			case GETFIELD_SHORT: {
				uint16_t a = vmPop();
				if (a == 0) {
					throwException(NULLPOINTER_EXCEPTION);
					break;
				}
				vmPush(heapGet(a, param));
				break;
			}
			case SETFIELD_SHORT: {
				uint16_t n = vmPop();
				uint16_t a = vmPop();
				if (a == 0) {
					throwException(NULLPOINTER_EXCEPTION);
					break;
				}
				heapSet(a, param, n);
				break;
			}
			case CALL_SHORT: {
				uint16_t addr = vmGetFuncCode(param) + codeOffset;
				call(addr);
				break;
			}
			case RET_SHORT: {
				ret();
				vmChangeStack(-param);
				if (ip == 0)
					return 0;
				break;
			}
			case RETI_SHORT: {
				uint16_t val = vmPop();
				ret();
				vmChangeStack(-param);
				vmPush(val);
				if (ip == 0)
					return 0;
				break;
			}
			case LIT_SHORT:
				vmPush(getUint16(constPool + param * 2));
				break;
			case ENTER_SHORT: {
				uint16_t link = vmPop();
				uint16_t ret = vmPop();
				vmChangeStack(param);
				vmPush(ret);
				vmPush(link);
				fp = stackPtr - stack;
				break;
			}
			case NEW_SHORT:
				_new(param);
				break;
			default:
				vmSetStatus(VMSTATUS_INVALID_BYTECODE);
				break;
			}
			continue;
		}
		switch (c) {
		case CALL: {
			uint16_t addr = vmGetFuncCode(vmReadByte(ip++)) + codeOffset;
			call(addr);
			break;
		}
		case VCALL: {
			uint8_t arg = vmReadByte(ip++);
			int argCnt = (arg >> 4) & 0xFF;
			int n = arg & 0x0F;
			int obj = stackPtr[argCnt - 1];
			if (obj == 0) {
				throwException(NULLPOINTER_EXCEPTION);
				break;
			}
			uint16_t func = getVMethod(heapGetTypeIdx(obj), n);
			call(func + codeOffset);
			break;
		}
		case THROW: {
			int16_t res = vmPop();
			throwException(res);
			break;
		}
		case NEW: {
			uint16_t n = vmReadByte(ip++);
			_new(n);
			break;
		}
		case LIT:
			vmPush(getUint16(constPool + vmReadByte(ip++) * 2));
			break;

		case NEWINTARR: {
			uint16_t sz = vmPop();
			_newArr(sz, 0);
			break;
		}
		case NEWOBJARR: {
			uint16_t sz = vmPop();
			_newArr(sz, 1);
			break;
		}
		case SETARRAY: {
			int16_t val = vmPop();
			uint16_t idx = vmPop();
			uint16_t a = vmPop();
			if (a == 0) {
				throwException(NULLPOINTER_EXCEPTION);
				break;
			}
			if (idx < 0 || idx >= heapGetArraySize(a)) {
				throwException(ARRAYINDEX_EXCEPTION);
				break;
			}
			heapSet(a, idx, val);
			break;
		}
		case GETARRAY: {
			uint16_t idx = vmPop();
			uint16_t a = vmPop();
			if (a == 0) {
				throwException(NULLPOINTER_EXCEPTION);
				break;
			}
			if (idx < 0 || idx >= heapGetArraySize(a)) {
				throwException(ARRAYINDEX_EXCEPTION);
				break;
			}
			vmPush(heapGet(a, idx));
			break;
		}
		case ARRAYLENGTH: {
			uint16_t a = vmPop();
			if (a == 0) {
				throwException(NULLPOINTER_EXCEPTION);
				break;
			}
			vmPush(heapGetArraySize(a));
			break;
		}
		case SETREG:
			_setReg(vmReadByte(ip++), vmPop());
			break;
		case GETREG:
			vmPush(_getReg(vmReadByte(ip++)));
			break;
//		case SETTIMER_S:
//			vmTimerCnts[vmReadByte(ip++)] = (int32_t) vmPop() * 1000;
//			break;
		case SETTIMER_MS: {
			uint16_t ms = vmPop();
			uint16_t obj = vmPop();
			if (obj == 0) {
				throwException(NULLPOINTER_EXCEPTION);
				break;
			}
			setTimer(obj, ms);
			break;
		}
		case STOPTIMER: {
			uint16_t obj = vmPop();
			if (obj == 0) {
				throwException(NULLPOINTER_EXCEPTION);
				break;
			}
			stopTimer(obj);
			break;
		}
		case SETTRIGGER: {
			uint16_t initVal = vmPop();
			uint16_t obj = vmPop();
			if (obj == 0) {
				throwException(NULLPOINTER_EXCEPTION);
				break;
			}
			setTrigger(obj, initVal);
			break;
		}
		case STOPTRIGGER: {
			uint16_t obj = vmPop();
			if (obj == 0) {
				throwException(NULLPOINTER_EXCEPTION);
				break;
			}
			stopTrigger(obj);
			break;
		}
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
			} else {
				vmPush(n1 * n2 / n3);
			}
			break;
		}
		case TRAP:
			break;
		default:
			vmSetStatus(VMSTATUS_INVALID_BYTECODE);
			break;
		}
	}

	return exception;
}

