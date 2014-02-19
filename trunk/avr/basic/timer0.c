#include "timer0.h"
#include <util/atomic.h>

static volatile char timerTicks;

char getClearTimerTicks() {
	char tticks;
	ATOMIC_BLOCK(ATOMIC_FORCEON) {
		tticks = timerTicks;
		timerTicks = 0;
	}
	return tticks;
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

ISR(TIMER0_OVF_vect) {
	static char n = TIME_UNIT / TIMER_PERIOD;
	TCNT0 = 255 - MODULO;
	ioMillisCli();
	if (!(--n)) {
		n = TIME_UNIT / TIMER_PERIOD;
		timerTicks++;
	}
}

void timer0_init() {

#if defined(__AVR_ATmega48__) || defined(__AVR_ATmega168__)

	TCCR0A=0x00;
	TCCR0B=PRESCALER;
	OCR0A=0x00;
	OCR0B=0x00;
	TIMSK0=0x01;

#else
#ifdef __AVR_ATmega8__

	TCCR0 = PRESCALER;
	TIMSK |= 0x01;

#else
#error
#endif
#endif
}

