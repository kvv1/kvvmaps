#ifndef HW_H_
#define HW_H_

#include <stdint.h>

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

void hwInit(uint8_t interrupts);
int rdByte(); // -1 on timeout
void wrByte(uint8_t b);

#define TIME_UNIT 10
char getClearTimerTicks();
void timer0Init();

#endif /* HW_H_ */
