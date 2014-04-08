#ifndef PWM_H_
#define PWM_H_

#include <stdint.h>

void setPWM(uint8_t port, uint16_t value);
uint16_t getPWM(uint8_t port);
void setOutput(uint8_t port, uint8_t state);
uint16_t getOutput(uint8_t port);
void handlePWM(int ms);
int getRelays();
void setRelays(uint8_t val);
void initPWM();

#endif /* PWM_H_ */
