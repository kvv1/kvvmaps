#include "ee.h"
#include <util/atomic.h>
#include <avr/io.h>

volatile uint16_t ee_magic;

#define MAGIC8 0x67

#include <avr/eeprom.h>

#if defined(__AVR_ATmega48__) || defined(__AVR_ATmega168__)
#define EEWE EEPE
#define EEMWE EEMPE
#define SPMCR SPMCSR
#endif


void _EEPROM_write(uint16_t uiAddress, uint8_t ucData, uint8_t magic) {

	while ((EECR & (1 << EEWE)) || (SPMCR & (1 << RWWSB)))
		; //ждем установки бита EEWE
	EEAR = uiAddress; //устанавливаем адрес

	EECR |= (1 << EERE);
	if (EEDR == ucData)
		return;

	EEDR = ucData; //записываем байт данных

	ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
	{
		if (ee_magic == MAGIC16) {
			EECR |= (1 << EEMWE); //устанавливаем EEMWE
			if (magic == MAGIC8)
				EECR |= (1 << EEWE); //устанавливаем EEWE
		}
	}
}

void EEPROM_writeByte(uint16_t uiAddress, uint8_t ucData) {
	_EEPROM_write(uiAddress, ucData, MAGIC8);
}


uint8_t EEPROM_readByte(uint16_t uiAddress) {
	while (EECR & (1 << EEWE))
		;
	EEAR = uiAddress;
	EECR |= (1 << EERE);
	return EEDR;
}

uint16_t EEPROM_readWord(uint16_t uiAddress) {
	return (EEPROM_readByte(uiAddress) << 8) | EEPROM_readByte(uiAddress + 1);
}

void EEPROM_readBlock(uint16_t uiAddress, int sz, uint8_t* dest) {
	while (sz--)
		*(dest++) = EEPROM_readByte(uiAddress++);
}

void EEPROM_writeWord(uint16_t uiAddress, uint16_t ucData) {
	EEPROM_writeByte(uiAddress, ucData >> 8);
	EEPROM_writeByte(uiAddress + 1, ucData);
}

void EEPROM_writeBlock(uint16_t uiAddress, int sz, uint8_t* src) {
	while (sz--)
		EEPROM_writeByte(uiAddress++, *(src++));
}

