/*
 * rs485.h
 *
 *  Created on: 26.08.2015
 *      Author: kvv
 */

#ifndef RS485_H_
#define RS485_H_

void rs485put(char c);
void rs485endOfPacket();
int8_t rs485sendBufferIfValid(uint16_t* id);


#endif /* RS485_H_ */
