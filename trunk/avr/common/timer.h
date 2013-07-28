/*
 * timer.h
 *
 *  Created on: 28.07.2013
 *      Author: kvv
 */

#ifndef TIMER_H_
#define TIMER_H_

#include "object.h"

//long getTimeMillis();
//long getTimeMillisCli();

typedef struct {
}*TIMERTYPE;

#define TIMER_NORMAL ((TIMERTYPE)0)
#define TIMER_PERIODIC ((TIMERTYPE)1)
#define TIMER_MIN ((TIMERTYPE)2)
#define TIMER_MAX ((TIMERTYPE)3)

void setTimer(ObjectHeader* obj, char cmd, TIMERTYPE type, long ms);
void setTimerParam(ObjectHeader* obj, char cmd, TIMERTYPE type, long ms, int param1,
		int param2);
void killTimer(ObjectHeader* obj, char cmd);

void handleTimers(int ms);

#endif /* TIMER_H_ */
