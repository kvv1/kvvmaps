#include <avr/io.h>
#include <util/delay.h>
#include <avr/interrupt.h>
#include "adc.h"

#include "audioInput.h"
#include "audioOutput.h"
#include "rs485.h"

#define UART_SPEED 9600UL

#define RS485_DDR DDRB
#define RS485_PORT PORTB
#define RS485_BIT 7

#define INDDR DDRC
#define INPIN PINC
#define INBIT 1

#define OUTDDR DDRC
#define OUTPORT PORTC
#define OUTBIT 4

#define AUDIOOUTDDR DDRC
#define AUDIOOUTPORT PORTC
#define AUDIOOUTBIT 3

//#define in() (INPIN & (1 << INBIT))
//#define in() readAdc()

#define out(b) do { if(b) OUTPORT |= (1 << OUTBIT); else OUTPORT &= ~(1 << OUTBIT); } while(0)
#define audioOut(b) do { if(b) AUDIOOUTPORT |= (1 << AUDIOOUTBIT); else AUDIOOUTPORT &= ~(1 << AUDIOOUTBIT); } while(0)

enum STATE {
	WAIT, RECEIVE_AUDIO, SEND_RS485, RECEIVE_RS485, SEND_AUDIO,
};

#define TIME_UNIT_MKS 34
static volatile uint16_t time;

//#define MODBUS_TIMEOUT_BYTE_TICKS (4 * 1000000 / 9600 / TIME_UNIT_MKS)
#define MODBUS_TIMEOUT_BYTE_TICKS 100
#define MODBUS_TIMEOUT_PACKET_TICKS 15000
//#define MODBUS_TIMEOUT_BYTE_TICKS (3 * 1000 / TIME_UNIT_MKS)
//#define MODBUS_TIMEOUT_PACKET_TICKS (20 * 1000 / TIME_UNIT_MKS)

static uint8_t onSampleIn(uint8_t in) {
	static uint16_t time1;
	static uint8_t prev;

	uint8_t dt = time - time1;
	if (prev != in || dt > 100) {
		time1 = time;
		prev = in;
		return dt;
	}
	return 255;
}

ISR(TIMER0_OVF_vect) {
	static uint8_t o;

	TCNT0 = 256 - 30;
	//TCNT0 = 256 - 18;

	time++;

	out(1);
	uint8_t in = readAdc();

	audioOut(o);

	uint8_t span = onSampleIn(in);

	out(0);

	sei();

	o = onSampleOut();

	if (span != 255)
		transitionReceived(span);
}

void put485ToAudio(uint16_t id) {
	while (UCSRA & (1 << RXC))
		UDR;

	int timeout = MODBUS_TIMEOUT_PACKET_TICKS;

	int time1;

	cli();
	time1 = time;
	sei();

	for (;;) {
		cli();
		int dt = time - time1;
		sei();
		if (dt > timeout)
			break;

		if (UCSRA & (1 << RXC)) {
			if (timeout != MODBUS_TIMEOUT_BYTE_TICKS) {
				audioPut(id >> 8);
				audioPut(id);
				audioPut((id ^ -1) >> 8);
				audioPut(id ^ -1);
			}

			audioPut(UDR);
			timeout = MODBUS_TIMEOUT_BYTE_TICKS;
			cli();
			time1 = time;
			sei();
		}
	}
}

int main() {
	uartInit();
	timer0Init();
	adcInit();

	OUTDDR |= (1 << OUTBIT);
	AUDIOOUTDDR |= (1 << AUDIOOUTBIT);

	//INDDR &= ~(1 << INBIT);

	sei();
	for (;;) {
		uint16_t id;
		if (rs485sendBufferIfValid(&id))
			put485ToAudio(id);
		while (!audioEmpty())
			;
	}
}

void byteReceived(uint8_t b) {
	rs485put(b);
}

void endOfInput() {
	rs485endOfPacket();
	//txBufReady = 1;
	//rs485buffer2audio();
}

void timer0Init() {
	TCCR0 = 2; // 1 mhz
	TIMSK |= 0x01;
}

void uartInit() {
	UCSRA = 0x00;
	UCSRB = (1 << RXEN) | (1 << TXEN);
	//UCSRB |= (1 << RXCIE) /*| (1 << TXCIE)*/;
	UCSRC = (1 << URSEL) | (1 << UCSZ1) | (1 << UCSZ0);

	unsigned long speed = (F_CPU / (16UL) / UART_SPEED) - 1UL;
	UBRRH = (speed >> 8) & 0xFF;
	UBRRL = speed & 0xFF;

	RS485_DDR |= (1 << RS485_BIT);
	RS485_PORT &= ~(1 << RS485_BIT);
}

