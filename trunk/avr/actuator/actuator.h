#ifndef _ACTUATOR_
#define _ACTUATOR_

typedef struct {
    ObjectHeader h;
    PORTPIN pinOut0;
    PORTPIN pinOut1;
    PORTPIN pinIn;
    int pos;
    int target;
    int range;
    int inc;
} Actuator;


void actuatorInit(Actuator* actuator, PORTPIN pinOut0, PORTPIN pinOut1, PORTPIN pinIn);
void actuatorGoto(Actuator* actuator, int target256);

#endif
