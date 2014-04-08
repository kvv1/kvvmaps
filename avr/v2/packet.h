#ifndef PACKET_H_
#define PACKET_H_

#include <stdint.h>

uint16_t sendByte(uint8_t b, uint16_t S);
uint16_t sendWord(uint16_t w, uint16_t S);
uint16_t sendPacketStart();
uint16_t sendPacketBodyPart(uint8_t* data, uint16_t len, uint16_t S);
void sendPacketEnd(uint16_t S);
void sendOk(uint8_t cmd);
void sendOk1(uint8_t cmd, uint8_t param);
void sendError(uint8_t cmd, uint8_t err) ;

#endif /* PACKET_H_ */
