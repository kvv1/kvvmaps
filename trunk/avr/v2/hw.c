#include <avr/io.h>
#include <util/atomic.h>
#include "hw.h"
#include "io.h"
#include "board.h"

#if(F_CPU == 1000000)
#define PRESCALER 3
#define MODULO 16
#define TIMER_PERIOD 1
//#define PRESCALER 4
//#define MODULO 39
//#define TIMER_PERIOD 10
#endif
#if(F_CPU == 2000000)
#define PRESCALER 3
#define MODULO 31
#define TIMER_PERIOD 1
//#define PRESCALER 4
//#define MODULO 78
//#define TIMER_PERIOD 10
#endif
#if(F_CPU == 4000000)
#define PRESCALER 4
#define MODULO 16
#define TIMER_PERIOD 1
//#define PRESCALER 5
//#define MODULO 39
//#define TIMER_PERIOD 10
#endif
#if(F_CPU == 8000000)
#define PRESCALER 4
#define MODULO 31
#define TIMER_PERIOD 1
//#define PRESCALER 5
//#define MODULO 78
//#define TIMER_PERIOD 10
#endif

#define UART_SPEED 9600UL

void hwInit(uint8_t interrupts) {
	UCSRA = 0x00;
	UCSRB = (1 << RXEN) | (1 << TXEN);

	if (interrupts)
		UCSRB |= (1 << RXCIE) | (1 << TXCIE);

#if defined(__AVR_ATmega48__) || defined(__AVR_ATmega168__)
	UCSRC = 0x06;
#else
#ifdef __AVR_ATmega8__
	UCSRC = 0x86;
#else
#error
#endif
#endif
	//RXCIE=1 и прерывания разрешены (бит I=1 в регистре SREG) : прерывание по завершению приёма по UART разрешено
	//TXCIE=1 и прерывания разрешены (бит I=1 в регистре SREG) : прерывание по завершению передачи по UART разрешено
	//UDRIE=1 и прерывания разрешены (бит I=1 в регистре SREG) : прерывание по опустошению регистра данных UART разрешено
	//RXEN=1 : активация приёмника, вывод D0 становится входом UART.
	//TXEN=1 : активация передатчика, вывод D1 становится выходом UART.
	//CHR9=1 : длина передаваемой посылки с становится равной 11 бит (9 бит данных + старт-стоповый бит + стоп-бит).
	//RXB8-расширенный стоп-бит
	//TXB8-расширенный стоп-бит
	//вычисляем значение регистра скорости передачи данных
	unsigned long speed = F_CPU / (16UL);
	speed = (speed / UART_SPEED) - 1UL;
	UBRRH = (speed >> 8) & 0xff;
	UBRRL = speed & 0xFF;

	RS485_DDR |= (1 << RS485_BIT);
	RS485_PORT &= ~(1 << RS485_BIT);

	RX_DDR &= ~(1 << RX_BIT);
	RX_PORT |= (1 << RX_BIT);

#if defined(__AVR_ATmega48__) || defined(__AVR_ATmega168__)
	TIMSK0 = 0x01;
#else
#ifdef __AVR_ATmega8__
	TIMSK |= 0x01;
#else
#error
#endif
#endif
}

#if(F_CPU == 1000000)
#define PRESCALER 3
#define MODULO 16
#define TIMER_PERIOD 1
//#define PRESCALER 4
//#define MODULO 39
//#define TIMER_PERIOD 10
#endif
#if(F_CPU == 2000000)
#define PRESCALER 3
#define MODULO 31
#define TIMER_PERIOD 1
//#define PRESCALER 4
//#define MODULO 78
//#define TIMER_PERIOD 10
#endif
#if(F_CPU == 4000000)
#define PRESCALER 4
#define MODULO 16
#define TIMER_PERIOD 1
//#define PRESCALER 5
//#define MODULO 39
//#define TIMER_PERIOD 10
#endif
#if(F_CPU == 8000000)
#define PRESCALER 4
#define MODULO 31
#define TIMER_PERIOD 1
//#define PRESCALER 5
//#define MODULO 78
//#define TIMER_PERIOD 10
#endif

static volatile char timerTicks;

char getClearTimerTicks() {
	char tticks;
	ATOMIC_BLOCK(ATOMIC_FORCEON) {
		tticks = timerTicks;
		timerTicks = 0;
	}
	return tticks;
}

ISR(TIMER0_OVF_vect) {
	static char n = TIME_UNIT / TIMER_PERIOD;
	TCNT0 = 255 - MODULO;
	ioMillis();
	if (!(--n)) {
		n = TIME_UNIT / TIMER_PERIOD;
		timerTicks++;
	}
}

void timer0Init() {

#if defined(__AVR_ATmega48__) || defined(__AVR_ATmega168__)

	TCCR0A = 0x00;
	TCCR0B = PRESCALER;
	OCR0A = 0x00;
	OCR0B = 0x00;
	TIMSK0 = 0x01;

#else
#ifdef __AVR_ATmega8__

	TCCR0 = PRESCALER;
	TIMSK |= 0x01;

#else
#error
#endif
#endif
}

