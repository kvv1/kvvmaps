#include "bl.h"
#include "io.h"

#include "packet.h"


uint16_t sendByte(uint8_t b, uint16_t S) {
	uartPutchar(b);
	return bl_crc16_step(b, S);
}

uint16_t sendWord(uint16_t w, uint16_t S) {
	S = sendByte(w >> 8, S);
	return sendByte(w, S);
}

uint16_t sendPacketStart() {
	return sendByte(bl_getAddr(), CRC16_INIT);
}

uint16_t sendPacketBodyPart(void* data, uint16_t len, uint16_t S) {
	uint8_t* data1 = (uint8_t*) data;
	while (len--)
		S = sendByte(*(data1++), S);
	return S;
}

void sendPacketEnd(uint16_t S) {
	uartPutchar(S);
	uartPutchar(S >> 8);
}

void sendOk(uint8_t cmd) {
	uint16_t S = sendPacketStart();
	S = sendByte(cmd, S);
	sendPacketEnd(S);
}

void sendOk1(uint8_t cmd, uint8_t param) {
	uint16_t S = sendPacketStart();
	S = sendByte(cmd, S);
	S = sendByte(param, S);
	sendPacketEnd(S);
}

void sendError(uint8_t cmd, uint8_t err) {
	uint16_t S = sendPacketStart();
	S = sendByte(cmd | 0x80, S);
	S = sendByte(err, S);
	sendPacketEnd(S);
}


