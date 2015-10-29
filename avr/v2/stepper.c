#include "stepper.h"
#include "regs.h"
#include "pin.h"
#include "board.h"
#include <avr/interrupt.h>

#define STEPPER_IDX_POS 0
#define STEPPER_IDX_CNT 2
#define STEPPER_IDX_EN 4
#define STEPPER_IDX_DIR 5
#define STEPPER_IDX_SPEED 6
#define STEPPER_IDX_ACCEL 7
#define STEPPER_IDX_CMD 8
#define STEPPER_IDX_IN 9
#define STEPPER_IDX_RUNNING 10

#define STEPPER_CMD_STOP 0
#define STEPPER_CMD_MOVE_IN1 1
#define STEPPER_CMD_MOVE_IN2 2

typedef struct {
	int32_t pos;
	uint32_t cnt;
	uint16_t stepMS;

	char running;
	uint16_t msCnt;
} StepperStruct;

static StepperStruct steppers[2];

//static PORTPIN pinsClk[] = { PIN_STEPPER_0_CLK, PIN_STEPPER_1_CLK };
//static PORTPIN pinsEn[] = { PIN_STEPPER_0_EN, PIN_STEPPER_1_EN };
static PORTPIN pinsDir[] = { PIN_STEPPER_0_DIR, PIN_STEPPER_1_DIR };
static PORTPIN pinsHome[] = { PIN_STEPPER_0_HOME, PIN_STEPPER_1_HOME };

void stepperInit() {
	confPin(PIN_STEPPER_0_HOME, PIN_IN, 1);
	confPin(PIN_STEPPER_1_HOME, PIN_IN, 1);

	confPin(PIN_STEPPER_0_DIR, PIN_OUT, 0);
	confPin(PIN_STEPPER_0_CLK, PIN_OUT, 0);

	confPin(PIN_STEPPER_1_DIR, PIN_OUT, 0);
	confPin(PIN_STEPPER_1_CLK, PIN_OUT, 0);
}

static inline char _stepperMS_cli(StepperStruct* stepper, char dir, char home) {
	if ((home && dir) || !stepper->cnt)
		stepper->running = 0;

	if (!stepper->running)
		return 0;

	if (stepper->msCnt)
		stepper->msCnt--;

	if (!stepper->msCnt) {
		stepper->msCnt = stepper->stepMS;
		stepper->cnt--;
		if (dir)
			stepper->pos--;
		else
			stepper->pos++;
		return 1;
	}
	return 0;
}

void stepperMS_cli() {
	char c0 = _stepperMS_cli(steppers, getPort(PIN_STEPPER_0_DIR),
			!getPin(PIN_STEPPER_0_HOME));
	char c1 = _stepperMS_cli(steppers + 1, getPort(PIN_STEPPER_1_DIR),
			!getPin(PIN_STEPPER_1_HOME));
	setPort(PIN_STEPPER_0_CLK, c0);
	setPort(PIN_STEPPER_1_CLK, c1);
}

static int32_t temp;

int16_t stepperGetReg(int stepperNo, int regOff) {
	StepperStruct* stepper = &steppers[stepperNo];
	int16_t res = 0;
	switch (regOff) {
	case STEPPER_IDX_POS:
		cli();
		temp = stepper->pos;
		sei();
		res = (int16_t) (temp >> 16);
		break;
	case STEPPER_IDX_POS + 1:
		res = (int16_t) (temp);
		break;
	case STEPPER_IDX_CNT:
		cli();
		temp = stepper->cnt;
		sei();
		res = (int16_t) (temp >> 16);
		break;
	case STEPPER_IDX_CNT + 1:
		res = (int16_t) (temp);
		break;
	case STEPPER_IDX_EN:
		//res = getPort(pinsEn[stepperNo]);
		break;
	case STEPPER_IDX_DIR:
		res = getPort(pinsDir[stepperNo]);
		break;
	case STEPPER_IDX_SPEED:
		cli();
		res = stepper->stepMS;
		sei();
		break;
	case STEPPER_IDX_IN:
		res = getPort(pinsHome[stepperNo]);
		break;
	case STEPPER_IDX_RUNNING:
		cli();
		res = stepper->running;
		sei();
		break;
	}
	return res;
}

void stepperSetReg(int stepperNo, int regOff, int16_t val) {
	StepperStruct* stepper = &steppers[stepperNo];
	switch (regOff) {
	case STEPPER_IDX_POS:
		temp = ((int32_t) val) << 16;
		break;
	case STEPPER_IDX_POS + 1:
		cli();
		stepper->pos = temp | (uint16_t) val;
		sei();
		break;
	case STEPPER_IDX_CNT:
		temp = ((int32_t) val) << 16;
		break;
	case STEPPER_IDX_CNT + 1:
		cli();
		stepper->cnt = temp | (uint16_t) val;
		sei();
		break;
	case STEPPER_IDX_EN:
		//setPort(pinsEn[stepperNo], val);
		break;
	case STEPPER_IDX_DIR:
		setPort(pinsDir[stepperNo], val);
		break;
	case STEPPER_IDX_SPEED:
		cli();
		stepper->stepMS = val;
		sei();
		break;
	case STEPPER_IDX_CMD:
		cli();
		switch (val) {
		case STEPPER_CMD_STOP:
			stepper->running = 0;
			break;
		case STEPPER_CMD_MOVE_IN1:
			stepper->msCnt = 0;
			stepper->running = 1;
			break;
		case STEPPER_CMD_MOVE_IN2:
			stepper->msCnt = 0;
			stepper->running = 1;
			break;
		}
		sei();
		break;
	}
}
