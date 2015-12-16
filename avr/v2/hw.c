#include <avr/io.h>
#include <util/atomic.h>
#include <stdlib.h>
#include "hw.h"
#include "io.h"
#include "board.h"
//#include "pin.h"
#include "stepper.h"
#include "ee.h"

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
	UCSRC = (1 << UCSZ1) | (1 << UCSZ0);
#else
#ifdef __AVR_ATmega8__
	UCSRC = (1 << URSEL) | (1 << UCSZ1) | (1 << UCSZ0);
#else
#error
#endif
#endif
	unsigned long speed = (F_CPU / (16UL) / UART_SPEED) - 1UL;
	UBRRH = (speed >> 8) & 0xFF;
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

static int8_t _checkHW() {
	if (!(RS485_DDR & (1 << RS485_BIT)))
		return 0;
	if (RX_DDR & (1 << RX_BIT))
		return 0;
	if (!(RX_PORT & (1 << RX_BIT)))
		return 0;
	if ((UCSRA & ((1 << U2X) | (1 << MPCM))) != 0)
		return 0;
	if ((UCSRB
			& ((1 << RXEN) | (1 << TXEN) | (1 << RXCIE) | (1 << TXCIE)
					| (1 << UDRIE) | (1 << UCSZ2)))
			!= ((1 << RXEN) | (1 << TXEN) | (1 << RXCIE) | (1 << TXCIE)))
		return 0;
	unsigned long speed = (F_CPU / (16UL) / UART_SPEED) - 1UL;
	if (UBRRH != ((speed >> 8) & 0xFF))
		return 0;
	if (UBRRL != (speed & 0xFF))
		return 0;

#if defined(__AVR_ATmega48__) || defined(__AVR_ATmega168__)
	if(TIMSK0 != 0x01)
	return 0;
	if ((UCSRC
					& ((1 << URSEL) | (1 << UMSEL) | (1 << UPM0) | (1 << UPM1)
							| (1 << USBS) | (1 << UCSZ1) | (1 << UCSZ0)))
			!= ((1 << URSEL) | (1 << UCSZ1) | (1 << UCSZ0)))
	return 0;
#else
#ifdef __AVR_ATmega8__
	if ((TIMSK & 0x01) != 0x01)
		return 0;
//	return 1;

	{
		unsigned char ucsrc;
		ucsrc = UBRRH; // read twice
		ucsrc = UCSRC; // read twice

		if ((ucsrc
				& ((1 << UMSEL) | (1 << UPM0) | (1 << UPM1) | (1 << USBS)
						| (1 << UCSZ1) | (1 << UCSZ0)))
				!= ((1 << UCSZ1) | (1 << UCSZ0)))
			return 0;
	}
#else
#error
#endif
#endif
	return 1;
}

int8_t checkHW() {
	int8_t res;
	ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {
		res = _checkHW();
	}
	return res;
}

static volatile char timerTicks;

char getClearTimerTicks() {
	char tticks;
	ATOMIC_BLOCK(ATOMIC_FORCEON) {
		tticks = timerTicks;
		timerTicks = 0;
	}
	return tticks;
}

#include "pin.h"

#define MS_IN_DAY (24L * 60 * 60 * 1000)
#define MS_IN_HOUR (60L * 60 * 1000)
//#define MS_IN_HOUR (30L * 1000)
#define MS_IN_MINUTE (60U * 1000)
#define MS_IN_SECOND 1000

static int correction;

int corr2;
int corr1;
int corrAcc;
int8_t timeInc;

static int16_t tickCnt;

static volatile uint32_t time;

static int32_t dist(int32_t ms1, int32_t ms2) {
	int32_t d = ms1 - ms2;

	if (d >= 0) {
		if (d < MS_IN_DAY / 2)
			return d;
		else
			return d - MS_IN_DAY;
	} else {
		if (d >= -MS_IN_DAY / 2)
			return d;
		else
			return MS_IN_DAY + d;
	}
}

void setTime(uint32_t ms) {
	static int16_t lastMS;

	return;

	if (!EEPROM_readByte(&eeData.timeCorrEn))
		return;

	cli();
	int32_t t = time;
	sei();

	int32_t dt = dist(ms, t);

	int ms16 = (int16_t) ms;

	cli();
	int16_t _tickCnt = tickCnt;
	sei();

	int dms = ms16 - lastMS;

	if (_tickCnt < MS_IN_SECOND * 10 && dms > 0
			&& abs(dt) < MS_IN_SECOND * 10) {
		int _corr1 = dms - _tickCnt;
		cli();
		corr2 = _tickCnt;
		if (_corr1 >= 0) {
			corr1 = _corr1;
			timeInc = 1;
		} else {
			corr1 = -_corr1;
			timeInc = -1;
		}
		correction = ms16 - (int16_t) time;
		sei();
	} else {
		cli();
		time = ms;
		sei();
	}

	lastMS = ms16;
	cli();
	tickCnt = 0;
	sei();
}

ISR(TIMER0_OVF_vect) {
	TCNT0 = 255 - MODULO;

	if (0) {

		if (tickCnt < MS_IN_SECOND * 10)
			tickCnt++;

		int8_t _timeInc = 1;

		corrAcc -= corr1;
		if (corrAcc < 0) {
			corrAcc += corr2;
			_timeInc += timeInc;
		} else {
			if (correction > 0) {
				_timeInc++;
				correction--;
			} else if (correction < 0) {
				_timeInc--;
				correction++;
			}
		}

		time += _timeInc;

		if (time >= MS_IN_DAY)
			time -= MS_IN_DAY;
	}

	static char n = TIME_UNIT / TIMER_PERIOD;

	setDDR(PIN_STEPPER_0_HOME, 1);
	setPort(PIN_STEPPER_0_HOME, 1);

	if (!(--n)) {
		n = TIME_UNIT / TIMER_PERIOD;
		timerTicks++;
	}

	stepperMS_cli();

	ioMillis_cli();
	setPort(PIN_STEPPER_0_HOME, 0);
}

int getCorr1() {
	int t;
	cli();
	t = corr1 * timeInc;
	sei();
	return t;
}

uint32_t getTime() {
	uint32_t t;
	cli();
	t = time;
	sei();
	return t;
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

