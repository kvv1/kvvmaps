#include <avr/pgmspace.h>
#include "pin.h"
#include "regs.h"
#include "board.h"
#include "inputs.h"

static PORTPIN pins[REG_IN_CNT] PROGMEM = { IN0, IN1, IN0, IN1};
#define PIN(n) ((PORTPIN)pgm_read_word(pins + (n)))

int8_t getInput(uint8_t n) {
	return getPin(PIN(n));
}

void setPullup(uint8_t n, uint8_t b) {
	setPort(PIN(n), b);
}

int getInputs() {
	char resp = 0;
	uint8_t n = REG_IN_CNT;
	while (n--) {
		resp <<= 1;
		resp |= getInput(n);
	}
	return resp;
}

void inputsInit() {
	uint8_t n = REG_IN_CNT;
	while (n--) {
		confPin(PIN(n), PIN_IN, 0);
	}
}
