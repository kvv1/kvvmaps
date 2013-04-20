#include "ee.h"
#include <util/atomic.h>

volatile uint16_t ee_magic;

#define MAGIC8 0x67

void _EEPROM_write(uint16_t uiAddress, uint8_t ucData, uint8_t magic) {
	while (EECR & (1 << EEWE))
		; //���� ��������� ���� EEWE
	EEAR = uiAddress; //������������� �����

	EECR |= (1 << EERE);
	if (EEDR == ucData)
		return;

	EEDR = ucData; //���������� ���� ������

	ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
	{
		if (ee_magic == MAGIC16) {
			EECR |= (1 << EEMWE); //������������� EEMWE
			if (magic == MAGIC8)
				EECR |= (1 << EEWE); //������������� EEWE
		}
	}
}

void EEPROM_write(uint16_t uiAddress, uint8_t ucData) {
	_EEPROM_write(uiAddress, ucData, MAGIC8);
}


uint8_t EEPROM_read(uint16_t uiAddress) {
	while (EECR & (1 << EEWE))
		;
	EEAR = uiAddress;
	EECR |= (1 << EERE);
	return EEDR;
}

uint16_t EEPROM_readWord(uint16_t uiAddress) {
	return (EEPROM_read(uiAddress) << 8) | EEPROM_read(uiAddress + 1);
}

void EEPROM_readBlock(uint16_t uiAddress, int sz, uint8_t* dest) {
	while (sz--)
		*(dest++) = EEPROM_read(uiAddress++);
}

void EEPROM_writeWord(uint16_t uiAddress, uint16_t ucData) {
	EEPROM_write(uiAddress, ucData >> 8);
	EEPROM_write(uiAddress + 1, ucData);
}

void EEPROM_writeBlock(uint16_t uiAddress, int sz, uint8_t* src) {
	while (sz--)
		EEPROM_write(uiAddress++, *(src++));
}

