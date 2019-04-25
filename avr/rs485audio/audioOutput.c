#include <avr/io.h>
#include <util/delay.h>
#include <util/atomic.h>

#include "audioOutput.h"

#define BUF_SZ 64

static uint8_t buffer[BUF_SZ];
static volatile uint8_t cnt;
static uint8_t head;
static uint8_t tail;

static uint8_t bufferPut(uint8_t b) {
	cli();
	if (cnt >= BUF_SZ) {
		sei();
		return 0;
	}
	cnt++;
	sei();

	buffer[head++] = b;
	if (head == BUF_SZ)
		head = 0;

	return 1;
}

static uint8_t bufferGet() {
	cli();
	if (cnt == 0) {
		sei();
		return 0;
	}
	cnt--;
	sei();

	uint8_t res = buffer[tail++];
	if (tail == BUF_SZ)
		tail = 0;
	return res;
}

static uint8_t bufferAvialable() {
	return cnt;
}




//----    ----    ---- -
//    ----    ----    -

uint8_t onSampleOut() {
	static uint8_t div = 1;
	static uint8_t bitCnt;
	static uint8_t curByte;
	static uint8_t startBitSent;
	static uint8_t res;

	if (--div)
		return res;
	div = 4;

	if (!bitCnt) {
		if (!bufferAvialable()) {
			curByte = 0xC0;
			bitCnt = 8;
			startBitSent = 0;
		} else if (!startBitSent) {
			curByte = 0x80;
			bitCnt = 2;
			startBitSent = 1;
		} else {
			curByte = bufferGet();
			bitCnt = 16;
		}
	}

	res = curByte & 0x80;
	if (bitCnt & 1) {
		if (startBitSent)
			res ^= 0x80;
		curByte <<= 1;
	}
	bitCnt--;
	return res;
}

void audioPut(uint8_t b) {
	bufferPut(b);
}

int8_t audioEmpty() {
	return !bufferAvialable();
}
