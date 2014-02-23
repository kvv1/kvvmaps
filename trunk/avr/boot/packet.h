#ifndef PACKET_H_
#define PACKET_H_

#include <stdint.h>

uint16_t crc16_step(uint8_t c, uint16_t crc_val);
uint16_t crc16(uint8_t * buf, uint16_t nbytes);
uint16_t sendByte(uint8_t b, uint16_t S);
uint16_t sendPacketStart();
void sendPacketEnd(uint16_t S);
void sendOk(uint8_t cmd);
void sendError(uint8_t cmd, uint8_t err);

#endif /* PACKET_H_ */
