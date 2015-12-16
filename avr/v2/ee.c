#include "ee.h"
#include <util/atomic.h>
#include <avr/io.h>

EEMEM EEData eeData;

volatile uint16_t ee_magic;

#define MAGIC8 0x67

#if defined(__AVR_ATmega48__) || defined(__AVR_ATmega168__)
#define EEWE EEPE
#define EEMWE EEMPE
#define SPMCR SPMCSR
#endif

static void _EEPROM_write(void* dest, uint8_t ucData, uint8_t magic) {

	while ((EECR & (1 << EEWE)) || (SPMCR & (1 << RWWSB)))
		; //ждем установки бита EEWE
	EEAR = (uint16_t)dest; //устанавливаем адрес

	EECR |= (1 << EERE);
	if (EEDR == ucData)
		return;

	EEDR = ucData; //записываем байт данных

	ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {
		if (ee_magic == MAGIC16) {
			EECR |= (1 << EEMWE); //устанавливаем EEMWE
			if (magic == MAGIC8)
				EECR |= (1 << EEWE); //устанавливаем EEWE
		}
	}
}

void EEPROM_writeByte(void* dest, uint8_t ucData) {
	if (EEPROM_readByte(dest) != ucData)
		_EEPROM_write(dest, ucData, MAGIC8);
}

uint8_t EEPROM_readByte(void* src) {
	while (EECR & (1 << EEWE))
		;
	EEAR = (uint16_t)src;
	EECR |= (1 << EERE);
	return EEDR;
}

uint16_t EEPROM_readWord(void* src) {
	return (((uint16_t) EEPROM_readByte(src)) << 8) | (EEPROM_readByte((char*)src + 1) & 0xFF);
}

void EEPROM_writeWord(void* dest, uint16_t ucData) {
	EEPROM_writeByte(dest, ucData >> 8);
	EEPROM_writeByte((char*)dest + 1, ucData);
}

