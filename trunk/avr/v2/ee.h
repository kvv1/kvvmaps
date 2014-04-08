
#ifndef EE_H_
#define EE_H_

#include <stdint.h>
#include <avr/eeprom.h>

#define MAGIC16 0xE6C9

extern volatile uint16_t ee_magic;

void EEPROM_write(uint16_t uiAddress, uint8_t ucData);
uint8_t EEPROM_read(uint16_t uiAddress);
uint16_t EEPROM_readWord(uint16_t uiAddress);
void EEPROM_readBlock(uint16_t uiAddress, int sz, uint8_t* dest);
void EEPROM_writeWord(uint16_t uiAddress, uint16_t ucData);
void EEPROM_writeBlock(uint16_t uiAddress, int sz, uint8_t* src);

#define ee_8_decl(name) uint8_t get##name(); void set##name(uint8_t t)
#define ee_16_decl(name) uint16_t get##name(); void set##name(uint16_t t)

#define ee_8(name)\
static EEMEM uint8_t ee_##name;\
uint8_t get##name() {\
	return EEPROM_read((uint16_t)&ee_##name);\
}\
void set##name(uint8_t t) {\
	EEPROM_write((uint16_t)&ee_##name, t);\
}

#define ee_16(name)\
static EEMEM uint16_t ee_##name;\
uint16_t get##name() {\
	return EEPROM_readWord((uint16_t)&ee_##name);\
}\
void set##name(uint16_t t) {\
	EEPROM_writeWord((uint16_t)&ee_##name, t);\
}

#endif /* EE_H_ */
