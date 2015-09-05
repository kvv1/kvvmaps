#include <avr/io.h>

#include "audioInput.h"

#define HALF_BIT_LEN 4

#define WAIT_LONG 0
#define WAIT_SHORT 1
#define WORK 2

static uint8_t state = WAIT_LONG;
static uint8_t lastBit;

static uint8_t byteAccum;
static uint8_t bitCnt;

static void bitReceived(uint8_t bit) {
	byteAccum <<= 1;
	byteAccum |= bit;
	bitCnt++;
	if (bitCnt == 8) {
		bitCnt = 0;
		byteReceived(byteAccum);
	}
}

static void reset() {
	state = WAIT_LONG;
	endOfInput();
}

void transitionReceived(uint8_t cnt) {
	static uint8_t len;

	cnt = (cnt + HALF_BIT_LEN / 2) / HALF_BIT_LEN;

	switch (state) {
	case WAIT_LONG:
		if (cnt == 4)
			state = WAIT_SHORT;
		if (cnt > 4)
			reset();
		return;
	case WAIT_SHORT:
		if (cnt == 4)
			return;
		if (cnt == 1) {
			bitCnt = 0;
			lastBit = 1;
			len = 0;
			state = WORK;
			return;
		}
		reset();
		return;
	case WORK:
		if (cnt == 0 || cnt > 2) {
			reset();
			return;
		}

		len += cnt;

		if (len > 2) {
			reset();
			return;
		}

		lastBit ^= 1;
		if (len == 2) {
			len = 0;
			bitReceived(lastBit);
		}
		return;
	default:
		return;
	}
}

