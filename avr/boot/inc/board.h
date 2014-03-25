#ifndef BOARD_H_
#define BOARD_H_

#define OUT0 P_D2
#define OUT1 P_D3
#define OUT2 P_D4
#define OUT3 P_B6
#define OUT4 P_B2

#define IN0 P_C2
#define IN1 P_C1

#define ADC0 2
#define ADC1 1
#define ADC2 4
#define ADC3 3

#define RS485_DDR DDRB
#define RS485_PORT PORTB
#define RS485_BIT 7

#define RX_DDR DDRD
#define RX_PORT PORTD
#define RX_BIT 0

#define PIN_MARS_RTS P_D6

#define PIN_1W P_D5
#define PIN_1W_1 P_C1

#define INT_VREF_TYPE 0xC0 // INTERNAL
#define AVCC_VREF_TYPE 0x40 // AVCC



#endif /* BOARD_H_ */
