#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/sleep.h>

#include <util/delay.h>

#include "1w.h"
#include "myio.h"

int main(void) {
	PORTB = 0x00;
	DDRB = 0x00;
	PORTC = 0x00;
	DDRC = 0x00;
	PORTD = 0x01;
	DDRD = 0x00;

	TCCR0 = 0x00;
	TCNT0 = 0x00;

	TCCR1A = 0x00;
	TCCR1B = 0x00;
	TCNT1H = 0x00;
	TCNT1L = 0x00;
	ICR1H = 0x00;
	ICR1L = 0x00;
	OCR1AH = 0x00;
	OCR1AL = 0x00;
	OCR1BH = 0x00;
	OCR1BL = 0x00;

	ASSR = 0x00;
	TCCR2 = 0x00;
	TCNT2 = 0x00;
	OCR2 = 0x00;

// External Interrupt(s) initialization
// INT0: Off
// INT1: Off
	MCUCR = 0x00;

// Timer(s)/Counter(s) Interrupt(s) initialization
	TIMSK = 0x00;

#define FOSC 8200000L
#define BAUD 9600
#define MYUBRR ((FOSC)/16/(BAUD)-1)
	UBRRH = (unsigned char) (MYUBRR >> 8);
	UBRRL = (unsigned char) MYUBRR;

// Analog Comparator initialization
// Analog Comparator: Off
// Analog Comparator Input Capture by Timer/Counter 1: Off
	ACSR = 0x80;
	SFIOR = 0x00;

	DDRB = 0x80;
	PORTB = 0x80;

	uart_init();

	sei();

	print0("simple\n");

	//vmMain();

	while (1) {
		_delay_ms(500);

		print0("012345678901234567890123456789abcdefgh\n");

		ds18b20_step(500);
		print1("%d ", temperature);
//		int t = getTemperature();
//		print1("%d\n", t);
	};

	return 0;
}

void handleRxCmd(char* cmd) {
}

void createObjects() {

}

