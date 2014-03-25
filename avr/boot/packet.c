#include <avr/io.h>
#include "packet.h"
#include "hw.h"
#include "inc/bl.h"

uint16_t crc16_step(uint8_t c, uint16_t crc_val) {
	crc_val ^= (uint16_t) c;

	char j = 8;
	while (j--) {
		uint8_t oldval = crc_val;
		crc_val >>= 1;
		if (oldval & 1)
			crc_val ^= 0xa001;
	}

	return crc_val;
}

uint16_t crc16(uint8_t * buf, uint16_t nbytes) {
	uint16_t crc_val = CRC16_INIT;
	while (nbytes--)
		crc_val = crc16_step(*buf++, crc_val);
	return crc_val;
}

uint16_t sendByte(uint8_t b, uint16_t S) {
	wrByte(b);
	return crc16_step(b, S);
}

uint16_t sendPacketStart() {
	startTX();
	return sendByte(MYADDR, CRC16_INIT);
}

void sendPacketEnd(uint16_t S) {
	wrByte(S);
	wrByte(S >> 8);
	stopTX();
}

void sendOk(uint8_t cmd) {
	uint16_t S = sendPacketStart();
	S = sendByte(cmd, S);
	sendPacketEnd(S);
}
/*
void sendOk(uint8_t cmd, uint8_t param) {
	uint16_t S = sendPacketStart();
	S = sendByte(cmd, S);
	S = sendByte(cmd, param);
	sendPacketEnd(S);
}
*/
void sendError(uint8_t cmd, uint8_t err) {
	uint16_t S = sendPacketStart();
	S = sendByte(cmd | 0x80, S);
	S = sendByte(err, S);
	sendPacketEnd(S);
}

