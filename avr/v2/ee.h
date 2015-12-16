
#ifndef EE_H_
#define EE_H_

#include <stdint.h>
#include <avr/eeprom.h>

#include "regs.h"

#define MAGIC16 0xE6C9

extern volatile uint16_t ee_magic;

void EEPROM_writeByte(void* dest, uint8_t ucData);
uint8_t EEPROM_readByte(void* src);
uint16_t EEPROM_readWord(void* src);
void EEPROM_writeWord(void* dest, uint16_t ucData);

#define RULES_LEN 256
typedef struct {
	uint8_t resetByWd;
	uint8_t wdOnReceive;
	uint8_t adcconf;
	uint8_t respDelay;
	uint16_t _foo2;
	uint16_t _foo3;
	uint16_t _foo4;
	uint16_t _foo5;
	uint16_t _foo6;
	uint16_t _foo7;
	int16_t eepromRegisters[REG_EEPROM_CNT];
	uint16_t pwm[REG_RELAY_CNT];
	char rules[RULES_LEN];
	uint8_t timeCorrEn;
} EEData;

extern EEMEM EEData eeData;

#endif /* EE_H_ */
