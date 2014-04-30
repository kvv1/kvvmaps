#include <avr/io.h>
#include <stdlib.h>
#include <util/atomic.h>
#include <avr/interrupt.h>

#include "hw.h"
#include "io.h"
#include "board.h"

#define RXB8 1
#define TXB8 0
#define UPE 2
#define OVR 3
#define FE 4

#define FRAMING_ERROR (1<<FE)
#define PARITY_ERROR (1<<UPE)
#define DATA_OVERRUN (1<<OVR)
#define DATA_REGISTER_EMPTY (1<<UDRE)
#define RX_COMPLETE (1<<RXC)

#define RXBUFSIZE 64
#define TX_BUFFER_SIZE 64

static uint8_t rxBuf[RXBUFSIZE];
static volatile uint8_t rxIdx;
static volatile uint8_t rxBufReady;

static char receiveTimer;

void ioMillis() {
	ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {
		if (receiveTimer != 0)
			if (--receiveTimer == 0)
				rxBufReady = 1;
	}
}

uint8_t* getPacket(uint8_t* len) {
	ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {
		if (rxBufReady && rxIdx) {
			*len = rxIdx;
			return rxBuf;
		}
	}
	return NULL;
}

void startReceiving() {
	ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {
		rxIdx = 0;
		rxBufReady = 0;
	}
}

ISR (USART_RXC_vect) {
	uint8_t status = UCSRA;
	uint8_t data = UDR;

	if (!rxBufReady
			&& (status & (FRAMING_ERROR | PARITY_ERROR | DATA_OVERRUN)) == 0) {
		rxBuf[rxIdx] = data;
		if (rxIdx < RXBUFSIZE - 1)
			rxIdx++;
		receiveTimer = 3;
	}
}

static uint8_t volatile tx_buffer[TX_BUFFER_SIZE];

static uint8_t volatile tx_wr_index, tx_rd_index, tx_counter;

int8_t transmitting() {
	return RS485_PORT & (1 << RS485_BIT);
}

void waitTransmitted() {
	while (transmitting())
		;
}

ISR (USART_TXC_vect) {
	if (tx_counter) {
		--tx_counter;
		UDR = tx_buffer[tx_rd_index];
		if (++tx_rd_index == TX_BUFFER_SIZE)
			tx_rd_index = 0;
	} else {
		RS485_PORT &= ~(1 << RS485_BIT);
	}
}

void uartPutchar(uint8_t c) {
	while (tx_counter == TX_BUFFER_SIZE)
		;
	ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {
		RS485_PORT |= (1 << RS485_BIT);
		if (tx_counter || ((UCSRA & DATA_REGISTER_EMPTY) == 0)) {
			tx_buffer[tx_wr_index] = c;
			if (++tx_wr_index == TX_BUFFER_SIZE)
				tx_wr_index = 0;
			++tx_counter;
		} else
			UDR = c;
	}
}

