/*
 * bl.h
 *
 *  Created on: 22.02.2014
 *      Author: kvv
 */

#ifndef BL_H_
#define BL_H_

#include <avr/io.h>
#include <stdint.h>

#if defined(__AVR_ATmega168__)

#define BOOTSIZE 512 // in words
#else
#ifdef __AVR_ATmega8__

#define BOOTSIZE 512 // in words
#else
#error
#endif
#endif

#define CRC16_INIT 0xffff

#define BOOTLOADER_START (FLASHEND + 1 - (BOOTSIZE * 2))

#define GET_BIGENDIAN(p) ((*(uint8_t*)(p) << 8) | *((uint8_t*)(p) + 1))
//#define GET_BIGENDIAN(p) (((uint16_t)*(uint8_t*)(p) << 8) | *((uint8_t*)(p) + 1))

#define BSWAP_16(x) ( (uint8_t)((x) >> 8) | ((uint8_t)(x)) << 8 )
//#define BSWAP_16(x) ( ((uint16_t)(x) >> 8) | (((uint16_t)(x)) << 8 ))

typedef struct {
	uint8_t func;
	uint8_t data[];
} PDU;

typedef struct {
	uint8_t addr;
	PDU pdu;
} ADU;

#define F(num, rettype, name, argnames, argtypenames) \
		typedef rettype (*pf_##name)argtypenames; \
		static __inline__ rettype bl_##name argtypenames { \
			return ((pf_##name) (BOOTLOADER_START / 2 + (num)))argnames; \
		}

#define F_VOID(num, name, argnames, argtypenames) \
		typedef void (*pf_##name)argtypenames; \
		static __inline__ void bl_##name argtypenames { \
			((pf_##name) (BOOTLOADER_START / 2 + (num)))argnames; \
		}

F_VOID(0, main, (), ())
F(1, uint8_t, getAddr, (), ())
F(2, uint16_t, crc16_step, (c, crc_val), (uint8_t c, uint16_t crc_val))
F(3, uint16_t, crc16, (buf, nbytes), (uint8_t * buf, uint16_t nbytes))

#endif /* BL_H_ */
