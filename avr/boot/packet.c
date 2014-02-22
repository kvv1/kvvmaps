#include "packet.h"
#include "address.h"

#define CRC16_INIT 0xffff

static uint16_t crc16_step(uint8_t c, uint16_t crc_val) {
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

uint16_t sendByte(uint8_t b, uint16_t S, WR_BYTE wrByteFunc) {
	wrByteFunc(b);
	return crc16_step(b, S);
}

uint16_t sendPacketStart(WR_BYTE wrByteFunc) {
	return sendByte(MYADDR, CRC16_INIT, wrByteFunc);
}

void sendPacketEnd(uint16_t S, WR_BYTE wrByteFunc) {
	sendByte(S, S, wrByteFunc);
	sendByte(S >> 8, S, wrByteFunc);
}

void sendPacketEnd1(uint16_t S, WR_BYTE wrByteFunc) {
	sendByte(S, S, wrByteFunc);
	sendByte(S >> 8, S, wrByteFunc);
}

void sendOk(uint8_t cmd, WR_BYTE wrByteFunc) {
	uint16_t S = sendPacketStart(wrByteFunc);
	S = sendByte(cmd, S, wrByteFunc);
	sendPacketEnd(S, wrByteFunc);
}

void sendError(uint8_t cmd, uint8_t err, WR_BYTE wrByteFunc) {
	uint16_t S = sendPacketStart(wrByteFunc);
	S = sendByte(cmd | 0x80, S, wrByteFunc);
	S = sendByte(err, S, wrByteFunc);
	sendPacketEnd(S, wrByteFunc);
}

