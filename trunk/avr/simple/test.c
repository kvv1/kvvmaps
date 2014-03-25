#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/sleep.h>

#include <util/delay.h>

#include <util/atomic.h>

#include "1w.h"
#include "myio.h"
#include "ee.h"

#include "dht11.h"

int n;

int main(void) {
	PORTB = 0x00;
	DDRB = 0xFF;
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

	DDRB = (1 << 7) | (1 << 6);
	PORTB = (1 << 7);

	uart_init();

	sei();

	print0("simple\n");

//	ee_magic = MAGIC16;
//	EEPROM_write(300, 33);
//	ee_magic = 0;
//	char b = EEPROM_read(300);
//	print1("byte = %d\n", b);

//vmMain();
	_delay_ms(2000);

	print0("0 ");
	print1("%d ", 3 / n);
	print0("1 ");

	while (1) {
		//_delay_ms(50);

		//print0("012345678901234567890123456789abcdefgh\n");

		_delay_ms(10);
		ds18b20_step(0, 10);
		print1("%d ", w1_temp(0));

//		int t;
//		int h;
//		int res = dht_read(DHT22, &t, &h);
//		print1("%d   ", res);
//		print2("t=%d h=%d\n", t, h);

//		int t = oneWireGetTemperature(0);
//		print1("%d ", t);
//		t = oneWireGetTemperature(1);
//		print1("%d\n", t);

		//PORTB = PORTB ^ (1 << 6);

	};

	return 0;
}

void handleRxCmd(char* cmd) {
}

void createObjects() {

}

