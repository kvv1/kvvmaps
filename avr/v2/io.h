#ifndef IO_H_
#define IO_H_

#include <stdint.h>

void ioMillis();
uint8_t* getPacket(uint8_t* len);
void startReceiving();
void uartPutchar(uint8_t c);
void waitTransmitted();

#endif /* IO_H_ */
