#ifndef STEPPER_H_
#define STEPPER_H_

#include <stdint.h>

void stepperInit();
void stepperMS_cli();
int16_t stepperGetReg(int stepperNo, int regOff);
void stepperSetReg(int stepperNo, int regOff, int16_t val);

#endif /* STEPPER_H_ */
