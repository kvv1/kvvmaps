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

	switch (msg->cmd) {
	case MSG_CMD: {
		uint8_t* cmd = (uint8_t*) msg->param1;
		uint8_t cmdlen = msg->param2;
		handleCmd(cmd, cmdlen);
		ee_magic = 0;
		break;
	}
	}

	return 0;
}

static ObjectHeader obj = { handler };

void createObjects() {
	initCommands();
	initVM();
	ee_magic = 0;
}

void packetReceived(char* data, uint8_t len) {
	postMessage(&obj, MSG_CMD, (int) data, len);
}

