#include "common.h"
#include "actuator.h"

#define trace0(a)
#define trace1(a,b)

#define ACTUATOR_SENSOR_TIMEOUT 500



enum {
	MSG_ACTUATOR_INIT = MSG_INTERNAL,
	MSG_ACTUATOR_SENSOR,
	MSG_ACTUATOR_SENSOR_DOWN,
	MSG_ACTUATOR_SENSOR_UP,
	MSG_ACTUATOR_TIMER,
	MSG_ACTUATOR_TIMER_DOWN,
	MSG_ACTUATOR_TIMER_UP,

	MSG_ACTUATOR_TEST,

	MSG_ACTUATOR_GOTO
};

static void stop(Actuator* this) {
	this->inc = 0;
	setPort(this->pinOut0, 0);
	setPort(this->pinOut1, 0);
	killTimer(&this->h, MSG_ACTUATOR_TIMER);
	killTimer(&this->h, MSG_ACTUATOR_TIMER_DOWN);
	killTimer(&this->h, MSG_ACTUATOR_TIMER_UP);
}

static void up(Actuator* this) {
	setPort(this->pinOut0, 0);
	setPort(this->pinOut1, 1);
}

static void down(Actuator* this) {
	setPort(this->pinOut0, 1);
	setPort(this->pinOut1, 0);
}

static int calcTarget(Actuator* this, int target256) {
	return (int) ((long) target256 * (long) this->range >> 8);
}

static int handler(Message* msg) {
	ObjectHeader* h = msg->target;
	Actuator* this = (Actuator*) h;
	switch (msg->cmd) {
	case MSG_ACTUATOR_INIT:
		trace0("actuatorInit\r\n");
		stop(this);
		setPinListener(this->pinIn, h, MSG_ACTUATOR_SENSOR_DOWN);
		setTimer(h, MSG_ACTUATOR_TIMER_DOWN, TIMER_NORMAL,
				ACTUATOR_SENSOR_TIMEOUT);
		down(this);
		break;
	case MSG_ACTUATOR_SENSOR_DOWN:
		trace0("s_d\r\n");
		setTimer(h, MSG_ACTUATOR_TIMER_DOWN, TIMER_NORMAL,
				ACTUATOR_SENSOR_TIMEOUT);
		break;
	case MSG_ACTUATOR_TIMER_DOWN:
		trace0("t_d\r\n");
		stop(this);
		this->pos = 0;
		setPinListener(this->pinIn, h, MSG_ACTUATOR_SENSOR_UP);
		setTimer(h, MSG_ACTUATOR_TIMER_UP, TIMER_NORMAL,
				ACTUATOR_SENSOR_TIMEOUT);
		up(this);
		break;
	case MSG_ACTUATOR_SENSOR_UP:
		trace0("s_u\r\n");
		setTimer(h, MSG_ACTUATOR_TIMER_UP, TIMER_NORMAL,
				ACTUATOR_SENSOR_TIMEOUT);
		this->pos++;
		break;
	case MSG_ACTUATOR_TIMER_UP:
		trace0("t_u\r\n");
		stop(this);
		setPinListener(this->pinIn, h, MSG_ACTUATOR_SENSOR);
		this->range = this->pos;

		postMessage(h, MSG_ACTUATOR_GOTO, this->target, 0);

		break;
	case MSG_ACTUATOR_GOTO: {
		int targ;
		trace1("gt %d\r\n", msg->param1);

		this->target = msg->param1;
		if (!this->range)
			break;

		stop(this);
		targ = calcTarget(this, this->target);
		trace1("pos %d\r\n", this->pos);
		trace1("range %d\r\n", this->range);
		trace1("targ %d\r\n", targ);
		//this->target = 10;
		if (targ > this->pos) {
			trace0("u\r\n");
			this->inc = 1;
			setTimer(h, MSG_ACTUATOR_TIMER, TIMER_NORMAL,
					ACTUATOR_SENSOR_TIMEOUT);
			up(this);
		} else if (targ < this->pos) {
			trace0("d\r\n");
			this->inc = -1;
			setTimer(h, MSG_ACTUATOR_TIMER, TIMER_NORMAL,
					ACTUATOR_SENSOR_TIMEOUT);
			down(this);
		}
		break;
	}
	case MSG_ACTUATOR_SENSOR: {
		int targ = calcTarget(this, this->target);
		trace0("s\r\n");
		this->pos += this->inc;
		setTimer(h, MSG_ACTUATOR_TIMER, TIMER_NORMAL, ACTUATOR_SENSOR_TIMEOUT);
		if ((this->inc > 0 && this->pos >= targ)
				|| (this->inc < 0 && this->pos <= targ))
			stop(this);
		break;
	}
	case MSG_ACTUATOR_TIMER:
		trace0("t\r\n");
		stop(this);
		break;
		/*
		 case MSG_ACTUATOR_TEST:
		 setPort(this->pinOut1, !getPin(this->pinOut1));
		 setTimer(this, MSG_ACTUATOR_TEST, TIMER_NORMAL, 50);
		 break;
		 */
	}

	return 0;
}

//void xxx(PORTPIN pinOut0) {
//	setPort(pinOut0, 0);
//}
//
//void yyy(PORTPIN pinOut0) {
//	setPort1(pinOut0, 0);
//}

void actuatorGoto(Actuator* actuator, int target256) {
	postMessage(&actuator->h, MSG_ACTUATOR_GOTO, target256, 0);
}

void actuatorInit(Actuator* actuator, PORTPIN pinOut0, PORTPIN pinOut1,
		PORTPIN pinIn) {
	actuator->h.handler = handler;
	actuator->pinOut0 = pinOut0;
	actuator->pinOut1 = pinOut1;
	actuator->pinIn = pinIn;
	confPin(pinOut0, PIN_OUT, 0);
	confPin(pinOut1, PIN_OUT, 0);
	confPin(pinIn, PIN_IN, 1);
	postMessage(&actuator->h, MSG_ACTUATOR_INIT, 0, 0);
}

