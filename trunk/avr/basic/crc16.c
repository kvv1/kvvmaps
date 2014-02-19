#include "crc16.h"

uint16_t crc16_step(uint8_t c, uint16_t crc_val) {
	crc_val ^= (uint16_t) c;

	char j = 8;
	while (j--) {
		uint8_t carry = crc_val & 0x0001;
		crc_val >>= 1;
		if (carry)
			crc_val ^= 0xa001;
	}

	return crc_val;
}

uint16_t crc16(uint8_t * buf, int nbytes) {
	uint16_t crc_val = CRC16_INIT;
	while (nbytes--)
		crc_val = crc16_step(*buf++, crc_val);
	return crc_val;
}


