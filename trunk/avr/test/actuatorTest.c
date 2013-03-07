#include "common.h"
#include "actuator.h"

static Actuator obj;

void createObjects() {
    trace0("createObjects\r\n");
    actuatorInit(&obj, OUT0, OUT1, IN0);
}

void handleRxCmd(char* cmd) {}
