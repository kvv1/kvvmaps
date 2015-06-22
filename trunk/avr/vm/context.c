#include "context.h"
#include "vmstatus.h"
#include "interpreter.h"
#include "heap.h"
#include "config.h"
#include <string.h>
#include <stdint.h>

static void stepTimers(int step);

typedef struct {
	int8_t flag;
	int16_t val;
} TTEntry;

static TTEntry timers[MAXTIMERS];
static TTEntry triggers[MAXTRIGGERS];
static int16_t currentTT;

static void init() {
	initVars();
	heap_init();
	memset(timers, 0, sizeof(timers));
	memset(triggers, 0, sizeof(triggers));
	vmSetStatus(VMSTATUS_RUNNING);
}

void vmInit() {
	if (vmGetStatus() != VMSTATUS_STOPPED)
		return;
	init();
	vmExec(vmGetFuncCode(0));
	vmSetStatus(VMSTATUS_STOPPED);
}

void vmStart(int8_t b) {
	if (b) {
		init();
		vmExec(vmGetFuncCode(1));
	} else {
		vmSetStatus(VMSTATUS_STOPPED);
	}
}

void vmStep(int ms) {
	if (vmGetStatus() != VMSTATUS_RUNNING)
		return;
}

void gc() {
#if GC
	heapStartMark();
	int i;
	for (i = 0; i < vmGetRefsCnt(); i++)
		heapMark(_getReg(vmGetRefReg(i)));

	for (i = 0; i < MAXTIMERS; i++)
		heapMark(timers[i].val);

	for (i = 0; i < MAXTRIGGERS; i++)
		heapMark(triggers[i].val);

	int16_t* p;
	for (p = stackPtr; p < stack + STACK_SIZE; p++)
		heapMark(*p);

	heapMark(currentTT);

	heapMarkClosure();
	heapSweep();
#endif
}

#define TIMER_CNT_IDX  0
#define TIMER_RUN_FUNC_IDX  0

#define TRIGGER_VAL_IDX  0
#define TRIGGER_VAL_FUNC_IDX  0
#define TRIGGER_HANDLE_FUNC_IDX  1

static void arrayClear(TTEntry* arr, uint8_t sz, int val) {
	int8_t i;
	for (i = 0; i < sz; i++) {
		if (arr[i].val == val) {
			arr[i].val = 0;
			break;
		}
	}
}

static void arraySet(TTEntry* arr, uint8_t sz, int val) {
	arrayClear(arr, sz, val);
	int8_t i;
	for (i = 0; i < sz; i++) {
		if (arr[i].val == 0) {
			arr[i].val = val;
			arr[i].flag = 1;
			break;
		}
	}
}

void setTimer(int obj, uint16_t ms) {
	arraySet(timers, MAXTIMERS, obj);
	heapSet(obj, TIMER_CNT_IDX, ms);
}

void stopTimer(int obj) {
	arrayClear(timers, MAXTIMERS, obj);
	heapSet(obj, TIMER_CNT_IDX, 0);
}

void setTrigger(int obj, uint16_t initVal) {
	arraySet(triggers, MAXTRIGGERS, obj);
	heapSet(obj, TRIGGER_VAL_IDX, initVal);
}

void stopTrigger(int obj) {
	arrayClear(triggers, MAXTRIGGERS, obj);
	heapSet(obj, TRIGGER_VAL_IDX, 0);
}

static void stepTimers(int step) {
	uint8_t i;
	for (i = 0; i < MAXTIMERS; i++) {
		if (!timers[i].flag) {
			int16_t obj = timers[i].val;
			if (obj != 0) {
				uint16_t cnt = heapGet(obj, TIMER_CNT_IDX);
				cnt -= step;
				if (cnt <= 0)
					cnt = 0;
				heapSet(obj, TIMER_CNT_IDX, cnt);
				if (cnt == 0) {
					currentTT = obj;
					timers[i].val = 0;
					uint16_t func = getVMethod1(obj, TIMER_RUN_FUNC_IDX);
					vmExec1(func, obj);
				}
			}
		}
	}

	for (i = 0; i < MAXTRIGGERS; i++) {
		if (!triggers[i].flag) {
			int16_t obj = triggers[i].val;
			if (obj != 0) {
				int16_t oldVal = heapGet(obj, TRIGGER_VAL_IDX);
				uint16_t func = getVMethod1(obj, TRIGGER_VAL_FUNC_IDX);
				int16_t newVal = eval1(func, obj);
				if (newVal != oldVal) {
					heapSet(obj, TRIGGER_VAL_IDX, newVal);
					func = getVMethod1(obj, TRIGGER_HANDLE_FUNC_IDX);
					vmExec3(func, obj, oldVal, newVal);
				}
			}
		}
	}

	for (i = 0; i < MAXTIMERS; i++) {
		timers[i].flag = 0;
	}

	for (i = 0; i < MAXTRIGGERS; i++) {
		triggers[i].flag = 0;
	}

	currentTT = 0;
}
