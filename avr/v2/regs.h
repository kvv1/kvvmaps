#ifndef REGS_H_
#define REGS_H_

#include <stdint.h>

#include "ee.h"

#define REG_RELAY0 0
#define REG_RELAY1 1
#define REG_RELAY2 2
#define REG_RELAY3 3
#define REG_PWM0 4
#define REG_PWM1 5
#define REG_PWM2 6
#define REG_PWM3 7
#define REG_RELAY_CNT 4

#define REG_TEMP 9
#define REG_ADC_CONF 10
#define REG_RESET_BY_WD 11
#define REG_WD_ON_RECEIVE 12

#define REG_INPUTS 15

#define REG_ADC0 16
#define REG_ADC1 17
#define REG_ADC2 18
#define REG_ADC3 19
#define REG_ADC_CNT 4

#define REG_IN0 20
#define REG_IN1 21
#define REG_IN2 22
#define REG_IN3 23
#define REG_IN_CNT 4

#define REG_EEPROM0 24
#define REG_EEPROM1 25
#define REG_EEPROM2 26
#define REG_EEPROM3 27
#define REG_EEPROM_CNT 4

#define REG_RAM0 28
#define REG_RAM1 29
#define REG_RAM2 30
#define REG_RAM3 31
#define REG_RAM_CNT 4

#define REG_STEPPER0_START 64
#define REG_STEPPER0_END 80
#define REG_STEPPER1_START 80
#define REG_STEPPER1_END 96

#define REG_INPULLUP0 248
#define REG_INPULLUP1 249
#define REG_INPULLUP2 250
#define REG_INPULLUP3 251


#define REG_RULES_SIZE 256
#define REG_RULES_STATE 257
#define REG_RULES 258
#define REG_RULES_CNT 128

//void initCommands();

char getReg(uint16_t reg, int* val);
char setReg(uint16_t reg, int val);

ee_8_decl(resetByWd);
ee_8_decl(wdOnReceive);
ee_8_decl(adcconf);


#endif /* REGS_H_ */
