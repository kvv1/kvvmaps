#include "common.h"
#include "timer.h"

typedef struct {
	Message msg;
	char periodic;
	long period;
	long time;
} TIMER;

static TIMER timers[NTIMERS];


void handleTimers(int ms) {
	for (int i = 0; i < NTIMERS; i++) {
		TIMER* timer = &timers[i];
		if (timer->msg.target) {
			timer->time -= ms;
			if (timer->time <= 0) {
				Message msg = timer->msg;
				if (timer->periodic)
					timer->time += timer->period;
				else
					timer->msg.target = 0;
				msg.target->handler(&msg);
			}
		}
	}
}

void setTimerParam(ObjectHeader* obj, char cmd, TIMERTYPE type, long ms,
		int param1, int param2) {
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

