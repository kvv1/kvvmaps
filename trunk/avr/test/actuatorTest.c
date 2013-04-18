#include "common.h"
#include "actuator.h"
#include "1w.h"

static Actuator actuator;

#define TEMP_MAX 35
#define TEMP_MIN 25

static int handler(Message* msg) {
	static int tSaved = 1000;
	int t = getTemperature();
	int targ256;

	if (tSaved == 1000)
		tSaved = t;

	if (t > tSaved + 1)
		tSaved += 1;
	else if (t < tSaved - 1)
		tSaved -= 1;

	if (tSaved <= TEMP_MIN)
		targ256 = 0;
	else if (tSaved >= TEMP_MAX)
		targ256 = 256;
	else
		targ256 = 256 * (tSaved - TEMP_MIN) / (TEMP_MAX - TEMP_MIN);

	print1("t=%d ", t);
	print1("tsaved=%d ", tSaved);
	print1("targ=%d\n", targ256);

	actuatorGoto(&actuator, targ256);

	return 0;
}

static ObjectHeader tController = { handler };

void createObjects() {
	trace0("createObjects\r\n");
	actuatorInit(&actuator, OUT0, OUT1, IN0);
	setTimer(&tController, 0, TIMER_PERIODIC, 1000);
}

void handleRxCmd(char* cmd) {
}
