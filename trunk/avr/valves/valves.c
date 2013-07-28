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

void createObjects() {
	initCommands();
	initVM();
	ee_magic = 0;
}

void packetReceived(uint8_t* data, uint8_t len) {
	handleCmd(data, len);
	ee_magic = 0;
}

int main() {
	return commonMain();
}

