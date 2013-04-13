#ifndef EE_H_
#define EE_H_

#include "avr/eeprom.h"

#define ee_8_decl(name) uint8_t get##name(); void set##name(uint8_t t)
#define ee_16_decl(name) uint16_t get##name(); void set##name(uint16_t t)

#define ee_8(name)\
static EEMEM uint8_t ee_##name;\
uint8_t get##name() {\
	return eeprom_read_byte(&ee_##name);\
}\
void set##name(uint8_t t) {\
	eeprom_update_byte(&ee_##name, t);\
	getdummy(); \
}

#define ee_16(name)\
static EEMEM uint16_t ee_##name;\
uint16_t get##name() {\
	return eeprom_read_word(&ee_##name);\
}\
void set##name(uint16_t t) {\
	eeprom_update_word(&ee_##name, t);\
	getdummy(); \
}

#endif /* EE_H_ */
