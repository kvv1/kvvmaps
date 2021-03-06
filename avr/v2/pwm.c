#include <avr/pgmspace.h>
#include "ee.h"
#include "pwm.h"
#include "pin.h"
#include "board.h"
#include "regs.h"

static PORTPIN ports[REG_RELAY_CNT] PROGMEM = { OUT0, OUT1, OUT2, OUT3};

#define PORT(n) ((PORTPIN)pgm_read_word(ports + (n)))
#define PIN(n) ((PORTPIN)pgm_read_word(pins + (n)))

static uint8_t outState;
static uint16_t outCnt[REG_RELAY_CNT];

void initPWM() {
	uint8_t n = REG_RELAY_CNT;
	while (n--) {
		confPin(PORT(n), PIN_OUT, 0);
		setOutput(n, 0);
	}
}

void setPWM(uint8_t port, uint16_t value) {
	EEPROM_writeWord(eeData.pwm + port, value);
}

uint16_t getPWM(uint8_t port) {
	return EEPROM_readWord(eeData.pwm + port);
}

void setOutput(uint8_t port, uint8_t state) {
	uint8_t mask = 1 << port;
	if (state) {
		if (!(outState & mask)) {
			outState |= mask;
			outCnt[port] = 0;
			setPort(PORT(port), getPWM(port) & 0xFF);
		}
	} else {
		outState &= ~mask;
		setPort(PORT(port), 0);
	}
}

uint16_t getOutput(uint8_t port) {
	return (outState >> port) & 1;
}

void handlePWM(int ms) {
	static uint16_t pwmCnt;

	pwmCnt += ms;
	if (pwmCnt >= 1000) {
		pwmCnt -= 1000;

		for (uint8_t i = 0; i < REG_RELAY_CNT; i++) {
			int8_t s;
			outCnt[i]++;
			if (outCnt[i] >= (getPWM(i) >> 8))
				outCnt[i] = 0;
			s = outCnt[i] < (getPWM(i) & 0xFF);
			setPort(PORT(i), s && getOutput(i));
		}
	}
}


