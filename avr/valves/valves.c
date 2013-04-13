#include "common.h"
#include "packet.h"
#include "commands.h"
#include "valves.h"
#include "vm.h"

#include <util/delay.h>

#include "1w.h"

int getTemperature10() {
	int t = temperature;
	if (t == -9999)
		return TEMPERATURE_INVALID;
	return 10 * t;
}

static int handler(Message* msg) {
	ObjectHeader* this = msg->target;
	switch (msg->cmd) {
	case MSG_CMD: {
		char* cmd = (char*) msg->param1;
		char cmdlen = msg->param2;
		handleCmd(cmd, cmdlen);
		break;
	}
	case MSG_TIMER_WORK: {
		int t = getTemperature10();
		if (t != TEMPERATURE_INVALID) {
			if (t < getPrefTemp()) {
				setPort(OUT0, 1);
				setTimer(this, MSG_TIMER_STOP, TIMER_NORMAL, PULSE);
			}

			if (t > getPrefTemp()) {
				setPort(OUT1, 1);
				setTimer(this, MSG_TIMER_STOP, TIMER_NORMAL, PULSE);
			}

			if (t > getPrefTemp2())
				setPort(OUT2, 1);

			if (t <= getPrefTemp())
				setPort(OUT2, 0);
		}
		break;
	}
	case MSG_TIMER_STOP:
		setPort(OUT0, 0);
		setPort(OUT1, 0);
		break;
	}
	return 0;
}

static ObjectHeader obj = { handler };

void createObjects() {
	initCommands();
	initVM();
	if (getTempOn())
		startStopTemperatureControl(); // sets OUT0 = OUT1 = 0 !!!
}

void packetReceived(char* data, uint8_t len) {
	postMessage(&obj, MSG_CMD, (int) data, len);
}

void startStopTemperatureControl() {
	if (getTempOn()) {
		postMessage(&obj, MSG_TIMER_WORK, 0, 0);
		setTimer(&obj, MSG_TIMER_WORK, TIMER_PERIODIC, PERIOD);
	} else {
		killTimer(&obj, MSG_TIMER_WORK);
		killTimer(&obj, MSG_TIMER_STOP);
		postMessage(&obj, MSG_TIMER_STOP, 0, 0);
	}
}
