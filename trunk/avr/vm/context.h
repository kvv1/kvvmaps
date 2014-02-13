#ifndef CONTEXT_H_
#define CONTEXT_H_

#include <stdint.h>
#include <interpreter.h>

void vmInit();
void vmStart(int8_t b);
void vmStep(int ms);

void setTimer(int a, uint16_t ms);
void stopTimer(int a);
void setTrigger(int obj, uint16_t initVal);
void stopTrigger(int obj);

#endif /* CONTEXT_H_ */
