#include <avr/io.h>
#include <util/delay.h>
#include "hw.h"

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
#define TIMEOUT_US 4000

uint16_t startCnt;

void initHW() {
	UCSRA = 0x00;
	UCSRB = (1 << RXEN) | (1 << TXEN);
#if defined(__AVR_ATmega48__) || defined(__AVR_ATmega168__)
	UCSRC=0x06;
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
	DDRB = 0x80;
	PORTB = 0;

#if defined(__AVR_ATmega48__) || defined(__AVR_ATmega168__)
	TIMSK0=0x01;
#else
#ifdef __AVR_ATmega8__
	TIMSK |= 0x01;
#else
#error
#endif
#endif

//	initADC();
	startCnt = 0;
}

void startTX() {
	PORTB |= 0x80;
}

void stopTX() {
	_delay_ms(2);
	PORTB &= ~0x80;
}

//void incCnt() {
//	startCnt++;
//}

int rdByte() {
	int8_t cnt = TIMEOUT_US / WAIT_UNIT_US;
	while (!(UCSRA & (1 << RXC))) { //ждём байта данных с COM-порта
		if (cnt < 0)
			return -1;
		_delay_us(WAIT_UNIT_US);
		cnt--;
		if (startCnt < 0xFFFFU)
			startCnt++;
//		incCnt();
	}
	return (uint8_t) UDR;
}

void wrByte(uint8_t b) {
	while (!(UCSRA & (1 << UDRE)))
		;
	UDR = b;
}

