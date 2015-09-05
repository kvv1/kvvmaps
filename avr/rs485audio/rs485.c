#include <avr/io.h>

#define RS485_DDR DDRB
#define RS485_PORT PORTB
#define RS485_BIT 7

static char txBuf[256];
static int txBufSize;

void rs485sendBuffer() {
	if (txBufSize == 0)
		return;

//	rs485put(txBufSize);

	RS485_PORT |= (1 << RS485_BIT);

	for (int i = 0; i < txBufSize; i++) {
		while (!(UCSRA & (1 << UDRE)))
			;
		UDR = txBuf[i];
		UCSRA |= (1 << TXC);
	}
	while (!(UCSRA & (1 << TXC)))
		;
	RS485_PORT &= ~(1 << RS485_BIT);
	txBufSize = 0;
}

void rs485put(char c) {
	if (txBufSize < sizeof(txBuf))
		txBuf[txBufSize++] = c;
}

void rs485buffer2audio() {
	for (int i = 0; i < txBufSize; i++)
		audioPut(txBuf[i]);
}
