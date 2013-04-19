#include "common.h"
#include "packet.h"

static char addr;

void packetReceived(char* data, uint8_t len);

void handleRxCmd(char* data) {
	if (data[1] == 0 || data[1] == MY_ADDR) {
		char len = data[0];
		addr = data[1];
		if (fletchSum((unsigned char*) data, len - 1) == data[len - 1]) {
			eepromWriteAllowed = EE_MAGIC;
			packetReceived(data + 2, len - 3);
		}
	}
}

void sendByte(uint8_t b, uint8_t* S) {
	uart_putchar(b);
	addFletchSum(b, S);
}

void sendPacketStart(uint16_t len, uint8_t* S) {
	*S = 0;
	if(len <= 252) {
		sendByte(len + 3, S);
	} else {
		len += 5;
		sendByte(0, S);
		sendByte(len >> 8, S);
		sendByte(len, S);
	}
	uart_putchar(addr | 0x80);
	addFletchSum(addr | 0x80, S);
}

void sendPacketBodyPart(uint8_t* data, uint16_t len, uint8_t* S) {
	while (len--) {
		sendByte(*data, S);
		data++;
	}
}

void sendPacketEnd(uint8_t* S) {
	sendByte(*S, S);
}


void sendPacket(char* data, uint16_t len) {
	uint8_t S = 0;
	sendPacketStart(len, &S);
	sendPacketBodyPart(data, len, &S);
	sendPacketEnd(&S);
}

