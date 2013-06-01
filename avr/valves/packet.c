#include "common.h"
#include "packet.h"

static char targetAddr;

void packetReceived(char* data, uint8_t len);

void handleRxCmd(char* data, unsigned char len) {
	if (len < 3)
		return;
	if (data[0] == 0 || data[0] == MY_ADDR) {
		uint16_t sum = crc16(data, len - 2);
		if (data[len - 2] == (char) sum && data[len - 1] == (char) (sum >> 8)) {
			targetAddr = data[0];
			ee_magic = MAGIC16;
			packetReceived(data + 1, len - 3);
		}
	}
}

uint16_t sendByte(uint8_t b, uint16_t S) {
	if (targetAddr != 0)
		uart_putchar(b);
	return crc16_step(b, S);
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

