#include "packet.h"
#include "crc16.h"
#include "settings.h"
#include "myio.h"

static char targetAddr;

void handleRxCmd(char* data, unsigned char len) {
	if (len < 3)
		return;
	if (data[0] == 0 || data[0] == MY_ADDR) {
		uint16_t sum = crc16((uint8_t*) data, len - 2);
		if ((data[len - 2] == (char) sum)
				&& (data[len - 1] == (char) (sum >> 8))) {
			targetAddr = data[0];
			packetReceived(data + 1, len - 3);
		}
	}
}

uint16_t sendByte(uint8_t b, uint16_t S) {
	if (targetAddr != 0)
		uartPutchar(b);
	return crc16_step(b, S);
}

uint16_t sendWord(uint16_t w, uint16_t S) {
	S = sendByte(w >> 8, S);
	return sendByte(w, S);
}

uint16_t sendPacketStart() {
	return sendByte(targetAddr, CRC16_INIT);
}

uint16_t sendPacketBodyPart(uint8_t* data, uint16_t len, uint16_t S) {
	while (len--)
		S = sendByte(*(data++), S);
	return S;
}

void sendPacketEnd(uint16_t S) {
	sendByte(S, S);
	sendByte(S >> 8, S);
}

void sendPacket(uint8_t* data, uint16_t len) {
	uint16_t S = CRC16_INIT;
	S = sendPacketStart(S);
	S = sendPacketBodyPart(data, len, S);
	sendPacketEnd(S);
}

int fetch(void* a) {
	return (*((uint8_t*) a) << 8) | *((uint8_t*) a + 1);
}

