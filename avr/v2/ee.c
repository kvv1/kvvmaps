#include "ee.h"
#include <util/atomic.h>
#include <avr/io.h>

volatile uint16_t ee_magic;

#define MAGIC8 0x67

#if defined(__AVR_ATmega48__) || defined(__AVR_ATmega168__)
#define EEWE EEPE
#define EEMWE EEMPE
#define SPMCR SPMCSR
#endif

static void _EEPROM_write(uint16_t dest, uint8_t ucData, uint8_t magic) {

	while ((EECR & (1 << EEWE)) || (SPMCR & (1 << RWWSB)))
		; //ждем установки бита EEWE
	EEAR = dest; //устанавливаем адрес

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

void EEPROM_write(uint16_t dest, uint8_t ucData) {
	if (EEPROM_read(dest) != ucData)
		_EEPROM_write(dest, ucData, MAGIC8);
}

uint8_t EEPROM_read(uint16_t src) {
	while (EECR & (1 << EEWE))
		;
	EEAR = src;
	EECR |= (1 << EERE);
	return EEDR;
}

uint16_t EEPROM_readWord(uint16_t src) {
	return (((uint16_t) EEPROM_read(src)) << 8) | (EEPROM_read(src + 1) & 0xFF);
}

void EEPROM_readBlock(uint16_t src, int sz, uint8_t* dest) {
	while (sz--)
		*(dest++) = EEPROM_read(src++);
}

void EEPROM_writeWord(uint16_t dest, uint16_t ucData) {
	EEPROM_write(dest, ucData >> 8);
	EEPROM_write(dest + 1, ucData);
}

void EEPROM_writeBlock(uint16_t dest, int sz, uint8_t* src) {
	while (sz--)
		EEPROM_write(dest++, *(src++));
}

