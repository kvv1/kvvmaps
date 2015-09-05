#include <avr/io.h>
#include <util/delay.h>
#include <util/atomic.h>

#include "audioOutput.h"

#define TX_BUFFER_SIZE 100

static uint8_t volatile tx_buffer[TX_BUFFER_SIZE];

static uint8_t volatile tx_counter;

//----    ----    ---- -
//    ----    ----    -

uint8_t onSampleOut() {
	static uint8_t div;
	static uint8_t bitCnt;
	static uint8_t curByte;
	static uint8_t startBitSent;
	static uint8_t res;
	static uint8_t tx_rd_index;

	if (!div) {
		div = 4;
	}

	div--;

	if (!div) {
		if (!bitCnt) {
			if (!tx_counter) {
				curByte = 0x0F;
				//curByte = 0;
				bitCnt = 8;
				startBitSent = 0;
			} else if (!startBitSent) {
				curByte = 0x0F;
				bitCnt = 8;
				startBitSent = 1;
			} else if (startBitSent == 1) {
				curByte = 0x40;
				bitCnt = 2;
				startBitSent = 2;
			} else {
				--tx_counter;
				curByte = tx_buffer[tx_rd_index];
				if (++tx_rd_index == TX_BUFFER_SIZE)
					tx_rd_index = 0;
				bitCnt = 8;
			}
		}

		res = curByte & 0x80;
		curByte <<= 1;
		bitCnt--;
	}

	return res;
}

void audioPut(uint8_t b) {
	static uint8_t tx_wr_index;

	uint8_t b1 = 0;
	uint8_t b2 = 0;
	for (int i = 0; i < 4; i++) {
		if (b & 0x80) {
			b1 <<= 2;
			b1 |= 0x1;
		} else {
			b1 <<= 2;
			b1 |= 0x2;
		}
		b <<= 1;
	}

	if (tx_counter < TX_BUFFER_SIZE - 1) {
		tx_buffer[tx_wr_index++] = b1;
		if (tx_wr_index == TX_BUFFER_SIZE)
			tx_wr_index = 0;
		cli();
		tx_counter++;
		sei();
	}

	for (int i = 0; i < 4; i++) {
		if (b & 0x80) {
			b2 <<= 2;
			b2 |= 0x1;
		} else {
			b2 <<= 2;
			b2 |= 0x2;
		}
		b <<= 1;
	}

	if (tx_counter < TX_BUFFER_SIZE - 1) {
		tx_buffer[tx_wr_index++] = b2;
		if (tx_wr_index == TX_BUFFER_SIZE)
			tx_wr_index = 0;
		cli();
		tx_counter++;
		sei();
	}

}

