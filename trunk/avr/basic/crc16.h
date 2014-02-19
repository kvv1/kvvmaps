/*
 * crc16.h
 *
 *  Created on: 19.02.2014
 *      Author: kvv
 */

#ifndef CRC16_H_
#define CRC16_H_

#include <stdint.h>

#define CRC16_INIT 0xffff
uint16_t crc16_step(uint8_t c, uint16_t crc_val);
uint16_t crc16(uint8_t * buf, int nbytes);

#endif /* CRC16_H_ */
