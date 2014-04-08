#include <avr/io.h>
#include <util/delay.h>
#include "hw.h"
//#include "inc/bl.h"
#include "inc/board.h"
#include "utils.h"

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

#if defined(__AVR_ATmega48__) || defined(__AVR_ATmega168__)
#define UCSRA UCSR0A
#define UDR UDR0
#define UCSRB UCSR0B

#define RXCIE RXCIE0
#define TXCIE TXCIE0
#define RXEN RXEN0
#define TXEN TXEN0
#define RXC RXC0
#define UDRE UDRE0

#define UCSRC UCSR0C
#define UBRRH UBRR0H
#define UBRRL UBRR0L

#define USART_RXC_vect USART_RX_vect
#define USART_TXC_vect USART_TX_vect
#endif

void hwInit() {
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
	//RXCIE=1 � ���������� ��������� (��� I=1 � �������� SREG) : ���������� �� ���������� ����� �� UART ���������
	//TXCIE=1 � ���������� ��������� (��� I=1 � �������� SREG) : ���������� �� ���������� �������� �� UART ���������
	//UDRIE=1 � ���������� ��������� (��� I=1 � �������� SREG) : ���������� �� ����������� �������� ������ UART ���������
	//RXEN=1 : ��������� ��������, ����� D0 ���������� ������ UART.
	//TXEN=1 : ��������� �����������, ����� D1 ���������� ������� UART.
	//CHR9=1 : ����� ������������ ������� � ���������� ������ 11 ��� (9 ��� ������ + �����-�������� ��� + ����-���).
	//RXB8-����������� ����-���
	//TXB8-����������� ����-���
	//��������� �������� �������� �������� �������� ������
	unsigned long speed = F_CPU / (16UL);
	speed = (speed / UART_SPEED) - 1UL;
	UBRRH = (speed >> 8) & 0xff;
	UBRRL = speed & 0xFF;

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

	RS485_DDR |= (1 << RS485_BIT);
	RS485_PORT &= ~(1 << RS485_BIT);

	RX_DDR &= ~(1 << RX_BIT);
	RX_PORT |= (1 << RX_BIT);

}

void startTX() {
	PORTB |= 0x80;
}

void stopTX() {
	_delay_ms(2);
	PORTB &= ~0x80;
}

int rdByte() {
	int8_t cnt = TIMEOUT_US / WAIT_UNIT_US;
	while (!(UCSRA & (1 << RXC))) { //��� ����� ������ � COM-�����
		if (cnt < 0)
			return -1;
		_delay_us(WAIT_UNIT_US);
		cnt--;
		if (globals.startCnt < 0xFFFFU)
			globals.startCnt++;
	}
	return (uint8_t) UDR;
}

void wrByte(uint8_t b) {
	while (!(UCSRA & (1 << UDRE)))
		;
	UDR = b;
}

