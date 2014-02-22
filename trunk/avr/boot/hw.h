#ifndef HW_H_
#define HW_H_

#include <stdint.h>

#define WAIT_UNIT_US 100

void initHW();
int rdByte(); // -1 on timeout
void wrByte(uint8_t b);
void startTX();
void stopTX();

extern uint16_t startCnt;

#endif /* HW_H_ */
