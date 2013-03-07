#include "common.h"
#include <util/atomic.h>

typedef struct {
	Message msg;
//	ObjectHeader* obj;
//	char cmd;
//	int param1;
//	int param2;
	char periodic;
	long period;
	long time;
} TIMER;

static TIMER timers[NTIMERS];

void (*ledHandler)() = foo;

#ifdef SINGLE_MILLIS
void millis();
#else
void (*millis)() = foo;
#endif

static long time;

long getTimeMillisCli() {
	return time;
}

long getTimeMillis() {
	long res;
	ATOMIC_BLOCK(ATOMIC_FORCEON) {
		res = time;
	}
	return res;
}

static char timerTicks;

void handleTimers() {
	char tticks;
	ATOMIC_BLOCK(ATOMIC_FORCEON) {
		tticks = timerTicks;
		timerTicks = 0;
	}
	while (tticks) {
		int i;
		tticks--;
		for (i = 0; i < NTIMERS; i++) {
			TIMER* timer = &timers[i];
			if (timer->msg.target) {
				timer->time -= 10;
				if (timer->time <= 0) {
					Message msg = timer->msg;
					if (timer->periodic)
						timer->time += timer->period;
					else
						timer->msg.target = 0;
					msg.target->handler(&msg);
//					sendMessage(obj, cmd, param1, param2);
				}
			}
		}
	}
}

void setTimerParam(ObjectHeader* obj, char cmd, TIMERTYPE type, long ms, int param1, int param2) {
	int i;
	TIMER* freeTimer = 0;

	long ms1 = ms;

	for (i = 0; i < NTIMERS; i++) {
		TIMER* timer = &timers[i];
		long time = timer->time;
		if (timer->msg.target == obj && timer->msg.cmd == cmd) {
			if ((type == TIMER_MAX && time > ms)
					|| (type == TIMER_MIN && time < ms))
				ms1 = time;
			freeTimer = timer;
			break;
		} else if (!timer->msg.target) {
			freeTimer = timer;
		}
	}

	if (freeTimer) {
		freeTimer->msg.target = obj;
		freeTimer->msg.cmd = cmd;
		freeTimer->msg.param1 = param1;
		freeTimer->msg.param2 = param2;
		freeTimer->time = freeTimer->period = ms1;
		freeTimer->periodic = (type == TIMER_PERIODIC);
	}
	if (!freeTimer)
		trace0("no free timers");
}

void setTimer(ObjectHeader* obj, char cmd, TIMERTYPE type, long ms) {
	setTimerParam(obj, cmd, type, ms, 0, 0);
}

void killTimer(ObjectHeader* obj, char cmd) {
	int i;
	for (i = 0; i < NTIMERS; i++) {
		TIMER* timer = &timers[i];
		if (timer->msg.target == obj && timer->msg.cmd == cmd) {
			timer->msg.target = 0;
			break;
		}
	}
}

#if(F_CPU == 1000000)
#define PRESCALER 3
#define MODULO 16
#endif
#if(F_CPU == 2000000)
#define PRESCALER 3
#define MODULO 31
#endif
#if(F_CPU == 4000000)
#define PRESCALER 4
#define MODULO 16
#endif
#if(F_CPU == 8000000)
#define PRESCALER 4
#define MODULO 31
#endif


ISR(TIMER0_OVF_vect) {
	static char inHandler;
	static char n = 10;

	TCNT0 = 255 - MODULO;

	time++;

	millis();

	if (!(--n)) {
		n = 10;
		if (!inHandler) {
			inHandler = 1;
			timerTicks++;
			NONATOMIC_BLOCK(NONATOMIC_FORCEOFF) {
				ledHandler();
			}
			inHandler = 0;
		}
	}
}

void timer0_init(void) {
	// Timer/Counter 0 initialization
	// Clock source: System Clock
	// Clock value: 31,250 kHz
	// Mode: Normal top=FFh
	// OC0A output: Disconnected
	// OC0B output: Disconnected

#if defined(__AVR_ATmega48__) || defined(__AVR_ATmega168__)

	TCCR0A=0x00;
	TCCR0B=0x04;
	TCNT0=255-31;
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

