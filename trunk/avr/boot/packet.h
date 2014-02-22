#ifndef PACKET_H_
#define PACKET_H_

#include <stdint.h>

#ifndef __WR_BYTE__
#define  __WR_BYTE__
typedef void (*WR_BYTE) (uint8_t);
#endif

uint16_t crc16(uint8_t * buf, int nbytes);
uint16_t sendByte(uint8_t b, uint16_t S, WR_BYTE wrByteFunc);
uint16_t sendPacketStart(WR_BYTE wrByteFunc);
void sendPacketEnd(uint16_t S, WR_BYTE wrByteFunc);
void sendOk(uint8_t cmd, WR_BYTE wrByteFunc);
void sendError(uint8_t cmd, uint8_t err, WR_BYTE wrByteFunc);

#endif /* PACKET_H_ */
