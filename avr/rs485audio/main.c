#include <avr/io.h>
#include <util/delay.h>
#include <avr/interrupt.h>
#include "adc.h"

#include "audioInput.h"
#include "audioOutput.h"

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

static volatile uint8_t span;

static void onSampleIn(uint8_t in) {
	static uint8_t prev;
	static uint8_t cnt;

	if ((prev != in || cnt > 100) && span == 255) {
		span = cnt;
		cnt = 0;
		prev = in;
	}
	cnt++;
}

ISR(TIMER0_OVF_vect) {
	TCNT0 = 256 - 30;
	//TCNT0 = 256 - 18;

	out(1);
	uint8_t in = readAdc();

	uint8_t o = onSampleOut();
	audioOut(o);

	onSampleIn(in);

	out(0);
}

int main() {
	uartInit();
	timer0Init();
	adcInit();

	OUTDDR |= (1 << OUTBIT);
	AUDIOOUTDDR |= (1 << AUDIOOUTBIT);

	//INDDR &= ~(1 << INBIT);

	sei();
//	rs485put('a');
//	rs485put('b');
//	rs485put('c');
	//rs485sendBuffer();
	for (;;) {
		if (span != 255) {
			uint8_t sp = span;
			span = 255;
			transitionReceived(sp);
		}
		if (UCSRA & (1 << RXC)) {
			audioPut(UDR);
		}
	}
}

void byteReceived(uint8_t b) {
	rs485put(b);
}

void endOfInput() {
	//rs485buffer2audio();
	rs485sendBuffer();
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

