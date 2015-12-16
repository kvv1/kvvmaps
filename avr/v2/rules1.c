#include <stdint.h>
#include "ee.h"
#include "rules1.h"
#include "bl.h"
#include "regs.h"

static __attribute__ ((noinline)) uint8_t get(uint8_t off) {
	return EEPROM_readByte(eeData.rules + off);
}

static __attribute__ ((noinline)) uint16_t get16(uint8_t off) {
	return EEPROM_readWord(eeData.rules + off);
}

#define set(off, b) EEPROM_write(eeData.rules + (off), b)
#define set16(off, w) EEPROM_writeWord(eeData.rules + (off), w)

#define getCRC() EEPROM_readWord(eeData.rules + RULES_LEN - 2)
#define setCRC(crc) EEPROM_writeWord(eeData.rules + RULES_LEN - 2, crc)

#define STACK_SIZE 20
static int stack[STACK_SIZE];
static int* sp;
static int err;

#define ERR_OK 0
#define ERR_STACK_OVERFLOW 1
#define ERR_STACK_UNDERFLOW 2
#define ERR_STACK_BALANCE 3
#define ERR_CORRUPTED 4
#define ERR_UNKNOWN_BYTECODE 5
#define ERR_WRONG_REGISTER 6

static void push1(int n) {
	if (err != ERR_OK)
		return;
	if (sp <= stack) {
		err = ERR_STACK_OVERFLOW;
		return;
	}
	*(--sp) = n;
}

static int pop1() {
	if (err != ERR_OK)
		return 1;
	if (sp >= stack + STACK_SIZE) {
		err = ERR_STACK_UNDERFLOW;
		return 1;
	}
	return *(sp++);
}

#define MAX_QUICK 64
#define LIT_MASK 0x80
#define REG_MASK 0x40

#define LIT 1
#define REG 2

#define COND 3
#define COND1 4

#define EQ 5
#define NEQ 6

#define LT 7
#define LE 8
#define GT 9
#define GE 10

#define PLUS 11
#define MINUS 12
#define MUL 13
#define DIV 14
#define NOT 15
#define NEG 16

#define OR 17
#define AND 18
#define NE0 19
#define PAR 20

static int eval1(uint8_t a, uint8_t len) {
	err = ERR_OK;

	int tos = 0;

	sp = stack + STACK_SIZE;

	uint8_t a1 = a + len;

	while (a < a1) {
		uint8_t c = get(a++);

		uint8_t bcGroup = c & (~(MAX_QUICK - 1));

		if (bcGroup == LIT_MASK) {
			int n = c & (MAX_QUICK - 1);
			push1(tos);
			tos = n - MAX_QUICK / 2;
		} else if (bcGroup == REG_MASK) {
			int n = c & (MAX_QUICK - 1);
			int val = 0;
			char b = getReg(n, &val);
			if (!b)
				err = ERR_WRONG_REGISTER;
			push1(tos);
			tos = val;
		} else {
			switch (c) {
			case LIT: {
				int s = get16(a);
				a += 2;
				push1(tos);
				tos = s;
				break;
			}
			case REG: {
				int s = get16(a);
				a += 2;
				int val = 0;
				char b = getReg(s, &val);
				if (!b)
					err = ERR_WRONG_REGISTER;
				push1(tos);
				tos = val;
				break;
			}
			case COND: {
				uint8_t n = get(a++);
				int n1 = tos;
				tos = pop1();
				if (!n1)
					a += n;
				break;
			}
			case COND1: {
				uint8_t n = get(a++);
				a += n + 1;
				break;
			}
			case EQ:
				tos = tos == pop1();
				break;
			case NEQ:
				tos = tos != pop1();
				break;
			case LT:
				tos = pop1() < tos;
				break;
			case LE:
				tos = pop1() <= tos;
				break;
			case GT:
				tos = pop1() > tos;
				break;
			case GE:
				tos = pop1() >= tos;
				break;
			case PLUS:
				tos = pop1() + tos;
				break;
			case MINUS:
				tos = pop1() - tos;
				break;
			case MUL:
				tos = pop1() * tos;
				break;
			case DIV:
				tos = pop1() / tos;
				break;
			case NOT:
				tos = !tos;
				break;
			case NEG:
				tos = -tos;
				break;
			case OR: {
				uint8_t n = get(a++);
				tos = tos != 0;
				if (tos)
					a += n;
				else
					tos = pop1();
				break;
			}
			case AND: {
				uint8_t n = get(a++);
				if (!tos)
					a += n;
				else
					tos = pop1();
				break;
			}
			case NE0:
				tos = tos != 0;
				break;
			case PAR:
				break;
			default:
				err = ERR_UNKNOWN_BYTECODE;
				return 0;
			}
		}
		if (err != ERR_OK)
			return 0;
	}

	int n = tos;
	tos = pop1();

	if (err != ERR_OK)
		return 0;

	if (sp != stack + STACK_SIZE)
		err = ERR_STACK_BALANCE;

	return n;
}

static int getSize() {
	err = ERR_OK;
	uint8_t a = 0;
	uint16_t S = CRC16_INIT;

	if (get(a) == 255)
		return 0;

	for (;;) {
		uint8_t len = get(a);

		if (!len) {
			S = bl_crc16_step(get(a), S);
			if (S != get16(a + 1)) {
				err = ERR_CORRUPTED;
				return 0;
			}
			return a + 3;
		}

		uint8_t a1 = a + len;

		if (a1 < a || a1 >= RULES_LEN - 3) {
			err = ERR_CORRUPTED;
			return 0;
		}

		while (a < a1) {
			S = bl_crc16_step(get(a), S);
			a++;
		}
	}
	return 0;
}

int8_t rules1Step() {
	err = ERR_OK;

	int sz = getSize();
	if (sz == 0 || err != ERR_OK)
		return 0;

	uint16_t a = 0;

	for (;;) {
		uint8_t len = get(a);
//		uint8_t tag = get(a + 1);
		uint8_t on = get(a + 2);
		uint8_t reg = get(a + 3);

		if (!len)
			break;

		uint8_t a1 = a + len;

		if (on) {
			a += 4;

			while (a < a1) {
				uint8_t l = get(a);
				int val = eval1(a + 1, l - 1);
				if (err == ERR_OK) {
					setReg(reg, val);
					break;
				}
				a += l;
			}
		}

		a = a1;
	}

	return 1;
}

int getRules1Size() {
	return getSize();
}

void setRules1Word(int a, int w) {
	return set16(a * 2, w);
}

int getRules1Word(int a) {
	return get16(a * 2);
}

int getRulesState() {
	//getSize();
	return err;
}
