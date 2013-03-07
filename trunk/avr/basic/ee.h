#ifndef EE_H_
#define EE_H_

#define ee_8_decl(name) uint8_t get##name(); void set##name(uint8_t t)
#define ee_16_decl(name) uint16_t get##name(); void set##name(uint16_t t)

#define ee_8(name)\
static EEMEM uint8_t name;\
uint8_t get##name() {\
	return eeprom_read_byte(&name);\
}\
void set##name(uint8_t t) {\
	eeprom_write_byte(&name, t);\
}

#define ee_16(name)\
static EEMEM uint16_t name;\
uint16_t get##name() {\
	return eeprom_read_word(&name);\
}\
void set##name(uint16_t t) {\
	eeprom_write_word(&name, t);\
}

#endif /* EE_H_ */
