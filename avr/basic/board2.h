#include "pin.h"

#define BOARD2

#define OUT0 P_D2
#define OUT1 P_D3
#define OUT2 P_D4
#define OUT3 P_B6
#define OUT4 P_B2

#define KEYBOARD_CHAN 5

#define IN0 P_C2
#define IN1 P_C1

#define ADC0 2
#define ADC1 1
#define ADC2 4
#define ADC3 3

#define LED_GRP0 PORTC.0
#define LED_GRP1 PORTB.0
#define LED0 PORTB.2
#define LED1 PORTB.1
#define LED2 PORTD.7

#define LED_GRP0_DDR DDRC.0
#define LED_GRP1_DDR DDRB.0
#define LED0_DDR DDRB.2
#define LED1_DDR DDRB.1
#define LED2_DDR DDRD.7

#define PIN_1W P_D5
#define PIN_1W_1 P_C1

#define PIN_485 P_B7

#define PIN_RXD P_D0


#define PIN_MARS_RTS P_D6

#define INT_VREF_TYPE 0xC0 // INTERNAL
#define AVCC_VREF_TYPE 0x40 // AVCC

