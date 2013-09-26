#include <avr/io.h>
#include <util/atomic.h>
#include <util/delay.h>
#include "common.h"
#include "myio.h"
#include "ee.h"
#include "1w.h"

#include <avr/sleep.h>
/*
void addFletchSum(uint8_t c, uint8_t* S) {
	*S += c;
	if (*S < c)
		(*S)++;
}

unsigned char fletchSum(uint8_t *buf, uint8_t len) {
	uint8_t S = 0;
	for (; len > 0; len--) {
		uint8_t R = *buf++;
		S += R;
		if (S < R)
			S++;
	}
	//if(S = 255) S = 0;
	return S;
}
*/
uint16_t crc16_step(uint8_t c, uint16_t crc_val) {
	crc_val ^= (uint16_t) c;

	char j = 8;
	while (j--) {
		uint8_t carry = crc_val & 0x0001;
		crc_val >>= 1;
		if (carry)
			crc_val ^= 0xa001;
	}

	return crc_val;
}

uint16_t crc16(uint8_t * buf, int nbytes) {
	uint16_t crc_val = CRC16_INIT;
	while (nbytes--)
		crc_val = crc16_step(*buf++, crc_val);
	return crc_val;
}

void foo() {
}

void init_adc(char inputs, unsigned char vref_type);
void timer0_init();

int commonMain(void) {
	ee_magic = MAGIC16;

	//	set_sleep_mode(SLEEP_MODE_PWR_DOWN);
//	sleep_enable();
//	sleep_cpu();
//	sleep_disable();

#if defined(__AVR_ATmega48__) || defined(__AVR_ATmega168__)

	// Crystal Oscillator division factor: 1
#pragma optsize-
	CLKPR=0x80;
	CLKPR=0x00;
#ifdef _OPTIMIZE_SIZE_
#pragma optsize+
#endif

#endif

	// External Interrupt(s) initialization
	// INT0: Off
	// INT1: Off
	// Interrupt on any change on pins PCINT0-7: Off
	// Interrupt on any change on pins PCINT8-14: Off
	// Interrupt on any change on pins PCINT16-23: Off
#if defined(__AVR_ATmega48__) || defined(__AVR_ATmega168__)
	EICRA=0x00;
	EIMSK=0x00;
	PCICR=0x00;
#else
#ifdef __AVR_ATmega8__
	MCUCR = 0x00;
#else
#error
#endif
#endif

#if defined(__AVR_ATmega48__) || defined(__AVR_ATmega168__)
	// Timer/Counter 1 Interrupt(s) initialization
	TIMSK1=0x00;
	// Timer/Counter 2 Interrupt(s) initialization
	TIMSK2=0x00;
#endif

	// Analog Comparator initialization
	// Analog Comparator: Off
	// Analog Comparator Input Capture by Timer/Counter 1: Off
#if defined(__AVR_ATmega48__) || defined(__AVR_ATmega168__)
	ACSR=0x80;
	ADCSRB=0x00;
#else
#ifdef __AVR_ATmega8__
	ACSR = 0x80;
	SFIOR = 0x00;
#else
#error
#endif
#endif

	timer0_init();
	init_adc(ADC_CHANS, AVCC_VREF_TYPE);
	uart_init();

	sei();

	trace0("init\r\n");
	createObjects();

	while (1) {
		char tticks = getClearTimerTicks();
		while (tticks) {
			tticks--;
			ds18b20_step(0, TIME_UNIT);
			ds18b20_step(1, TIME_UNIT);
			vmStep(TIME_UNIT);
			handleTimers(TIME_UNIT);
			handlePWM(TIME_UNIT);
		}
		handleIO();
		handlePins();
		handleMessages();
//		if (getMessage(&msg)) {
//			if (msg.target)
//				msg.target->handler(&msg);
//		} else {
//			set_sleep_mode(SLEEP_MODE_IDLE);
//			sleep_enable();
//			sleep_cpu();
//			sleep_disable();
//		}
	}
	return 0;
}

