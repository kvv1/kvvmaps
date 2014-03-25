/*
 * myio.c
 *
 *  Created on: 04.03.2013
 *      Author: kvv
 */

#include <avr/io.h>
#include <stdlib.h>
#include <util/atomic.h>
#include <avr/interrupt.h>

#include "myio.h"

#include "settings.h"

#ifdef BINARY_DATA
#include "common.h"
#endif

#define BAUD 9600

#define RXB8 1
#define TXB8 0
#define UPE 2
#define OVR 3
#define FE 4
#define UDRE 5
#define RXC 7

#define FRAMING_ERROR (1<<FE)
#define PARITY_ERROR (1<<UPE)
#define DATA_OVERRUN (1<<OVR)
#define DATA_REGISTER_EMPTY (1<<UDRE)
#define RX_COMPLETE (1<<RXC)

#if defined(__AVR_ATmega48__) || defined(__AVR_ATmega168__)
#define UCSRA UCSR0A
#define UDR UDR0
#define UCSRB UCSR0B

#define RXCIE RXCIE0
#define TXCIE TXCIE0
#define RXEN RXEN0
#define TXEN TXEN0

#define UCSRC UCSR0C
#define UBRRH UBRR0H
#define UBRRL UBRR0L

#define USART_RXC_vect USART_RX_vect
#define USART_TXC_vect USART_TX_vect
#endif

void handleRxCmd(char* cmd, unsigned char len);

#define RXBUFSIZE 40
#define TX_BUFFER_SIZE 32

static char rxBuf[RXBUFSIZE];
static volatile unsigned char rxIdx;
static volatile char rxBufReady;

#ifdef BINARY_DATA
static char receiveTimer;
#endif

void ioMillisCli() {
#ifdef BINARY_DATA
	if (receiveTimer != 0)
		if (--receiveTimer == 0)
			rxBufReady = 1;
#endif
}

void handleIO() {
	if (rxBufReady) {
		handleRxCmd(rxBuf, rxIdx);
		rxIdx = 0;
		rxBufReady = 0;
	}
}

ISR (USART_RXC_vect) {
	char status = UCSRA;
	char data = UDR;

#ifdef BINARY_DATA
	if (!rxBufReady
			&& (status & (FRAMING_ERROR | PARITY_ERROR | DATA_OVERRUN)) == 0) {
		rxBuf[rxIdx] = data;
		if (rxIdx < RXBUFSIZE - 1)
			rxIdx++;
		receiveTimer = 3;
	}
#else
	if (!rxBufReady && (status & (FRAMING_ERROR | PARITY_ERROR | DATA_OVERRUN)) == 0) {
		if (data < ' ') {
			rxBufReady = 1;
		} else {
			rxBuf[rxIdx] = data;
			if (rxIdx < RXBUFSIZE - 1)
			rxIdx++;
		}
	}
#endif
}

static char volatile tx_buffer[TX_BUFFER_SIZE];

#if TX_BUFFER_SIZE<256
static unsigned char volatile tx_wr_index, tx_rd_index, tx_counter;
#else
static unsigned int volatile tx_wr_index,tx_rd_index,tx_counter;
#endif

ISR (USART_TXC_vect) {
	if (tx_counter) {
		--tx_counter;
		UDR = tx_buffer[tx_rd_index];
		if (++tx_rd_index == TX_BUFFER_SIZE)
			tx_rd_index = 0;
	} else {
		TX_END();
	}
}

void uartPutchar(char c) {
	while (tx_counter == TX_BUFFER_SIZE)
		;
	ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {
		TX_START();
		if (tx_counter || ((UCSRA & DATA_REGISTER_EMPTY) == 0)) {
			tx_buffer[tx_wr_index] = c;
			if (++tx_wr_index == TX_BUFFER_SIZE)
				tx_wr_index = 0;
			++tx_counter;
		} else
			UDR = c;
	}
}

static void uart_putchar_cr(char c) {
	if (c == '\n')
		uartPutchar('\r');
	uartPutchar(c);
}

void uart_init() {
	// USART initialization
	// Communication Parameters: 8 Data, 1 Stop, No Parity
	// USART Receiver: On
	// USART Transmitter: On
	// USART0 Mode: Asynchronous
	// USART Baud Rate: 9600
	UCSRA = 0x00;

	UCSRB = (1 << RXCIE) | (1 << TXCIE) | (1 << RXEN) | (1 << TXEN);
#if defined(__AVR_ATmega48__) || defined(__AVR_ATmega168__)
	UCSRC=0x06;
#else
#ifdef __AVR_ATmega8__
	UCSRC = 0x86;
#else
#error
#endif
#endif

//#define F (F_CPU + F_CPU / 80 * 4)
#define F F_CPU

#if(F_CPU == 1000000)
	UCSRA |= 0x02;
#define MYUBRR (F/8/(BAUD)-1)
#else
#define MYUBRR (F/16/(BAUD)-1)
#endif

	UBRRH = (unsigned char) (MYUBRR >> 8);
	UBRRL = (unsigned char) MYUBRR;

#ifdef PIN_485
	confPin(PIN_485, PIN_OUT, 0);
#endif
	confPin(PIN_RXD, PIN_IN, 1);

//	UCSRA=0x02;
//	UCSRB=0xD8;
//	UCSRC=0x86;
//	UBRRH=0x00;
//	UBRRL=0x0C;
}

void print2(char* format, int n1, int n2) {
	char* pc = format;
	int args[2];
	uint8_t idx = 0;
	args[0] = n1;
	args[1] = n2;
	while (*pc) {
		char c = *pc;
		if (c == '%') {
			char buf[10];
			char* pc1 = buf;
			itoa(args[idx++], buf, 10);
			while (*pc1)
				uart_putchar_cr(*(pc1++));
			pc += 2;
		} else {
			uart_putchar_cr(*(pc++));
		}
	}
}

void print1(char* format, int n1) {
	print2(format, n1, 0);
}

void print0(char* format) {
	print1(format, 0);
}

