#include "context.h"
#include "vmstatus.h"
#include "interpreter.h"
#include "heap.h"
#include "array.h"

static Array timers;
static Array triggers;

static int stepTimers(int step);

void vmInit() {
	if (vmGetStatus() != VMSTATUS_STOPPED)
		return;
	initVars();
	heap_init();
	array_init(&timers, 1);
	array_init(&triggers, 1);
	vmSetStatus(VMSTATUS_RUNNING);
	vmExec(vmGetFuncCode(0));
	vmSetStatus(VMSTATUS_STOPPED);
}

static void vmMain() {
	initVars();
	heap_init();
	array_init(&timers, 1);
	array_init(&triggers, 1);
	vmSetStatus(VMSTATUS_RUNNING);
	vmExec(vmGetFuncCode(1));
}

void vmStart(int8_t b) {
	if (b) {
		vmMain();
	} else {
		vmSetStatus(VMSTATUS_STOPPED);
	}
}

void vmStep(int ms) {
	if (stepTimers(ms))
		gc();
	if (vmGetStatus() != VMSTATUS_RUNNING)
		return;
}

void gc() {
	heapStartMark();
	int i;
	for (i = 0; i < vmGetRefsCnt(); i++)
		heapMark(_getReg(vmGetRefReg(i)));

	heapMark(timers.a);
	heapMark(triggers.a);

	int16_t* p;
	for (p = stackPtr; p < stack + STACK_SIZE; p++)
		heapMark(*p);

	heapMarkClosure();
	heapSweep();
}

#define TIMER_CNT_IDX  0
#define  TIMER_RUN_FUNC_IDX  0

#define  TRIGGER_VAL_IDX  0
#define  TRIGGER_VAL_FUNC_IDX  0
#define  TRIGGER_HANDLE_FUNC_IDX  1

void setTimer(int obj, uint16_t ms) {
	array_clear(&timers, obj);
	array_add(&timers, obj);
	heapSet(obj, TIMER_CNT_IDX, ms);
}

void stopTimer(int obj) {
	array_clear(&timers, obj);
	heapSet(obj, TIMER_CNT_IDX, 0);
}

void setTrigger(int obj, uint16_t initVal) {
	array_clear(&triggers, obj);
	array_add(&triggers, obj);
	heapSet(obj, TRIGGER_VAL_IDX, initVal);
}

void stopTrigger(int obj) {
	array_clear(&triggers, obj);
	heapSet(obj, TRIGGER_VAL_IDX, 0);
}

static int timerStep(int obj, int step) {
	uint16_t cnt = heapGet(obj, TIMER_CNT_IDX);
	cnt -= step;
	if (cnt <= 0)
		cnt = 0;
	heapSet(obj, TIMER_CNT_IDX, cnt);
	if (cnt == 0) {
		uint16_t func = getVMethod(heapGetTypeIdx(obj), TIMER_RUN_FUNC_IDX);
		vmExec1(func, obj);
	}
	return cnt == 0;
}

static void triggerStep(int obj) {
	int16_t oldVal = heapGet(obj, TRIGGER_VAL_IDX);
	uint16_t func = getVMethod(heapGetTypeIdx(obj), TRIGGER_VAL_FUNC_IDX);
	int16_t newVal = eval1(func, obj);
	if (newVal != oldVal) {
		heapSet(obj, TRIGGER_VAL_IDX, newVal);
		func = getVMethod(heapGetTypeIdx(obj), TRIGGER_HANDLE_FUNC_IDX);
		vmExec3(func, obj, oldVal, newVal);
	}
}

static int stepTimers(int step) {
	int sz = array_size(&timers);
	int gc = 0;
	int i;
	for (i = 0; i < sz; i++) {
		short obj = array_getAt(&timers, i);
		if (obj != 0) {
			if (timerStep(obj, step)) {
				array_setAt(&timers, i, 0);
				gc = 1;
			}
		}
	}

	sz = array_size(&triggers);
	for (i = 0; i < sz; i++) {
		short obj = array_getAt(&triggers, i);
		if (obj != 0) {
			triggerStep(obj);
			gc = 1;
		}
	}

	array_compact(&timers);
	return gc;
}
