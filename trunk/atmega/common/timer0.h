#ifndef __TIMER0__
#define __TIMER0__

#include "object.h"

void timer0_init();

#pragma used+
long getTimeMillis();
long getTimeMillisCli();

#ifndef SINGLE_MILLIS
extern void (*millis)();
#endif

#define TIMER_NORMAL 0
#define TIMER_PERIODIC 1
#define TIMER_MIN 2
#define TIMER_MAX 3

void setTimer(ObjectHeader* obj, char cmd, char type, long ms);
void setTimerParam(ObjectHeader* obj, char cmd, char type, long ms, int param);
void killTimer(ObjectHeader* obj, char cmd);
#pragma used-

#endif
