/*
 * bl.h
 *
 *  Created on: 22.02.2014
 *      Author: kvv
 */

#ifndef BL_H_
#define BL_H_

#define BOOTSIZE 1024 // in words
#define BOOTLOADER_START (FLASHEND + 1 - (BOOTSIZE * 2))

typedef unsigned char byte;

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

#ifndef __WR_BYTE__
#define  __WR_BYTE__
typedef void (*WR_BYTE) (uint8_t);
#endif

F_VOID(1, bootloader, (), ())
F(2, byte*, getModbusData, (buffer, len, func, dataLen), (byte* buffer, byte len, byte* func,byte* dataLen))
F(3, uint16_t, crc16, (buf, nbytes), (uint8_t * buf, int nbytes))
F(4, uint16_t, sendByte, (b, S, wrByteFunc), (uint8_t b, uint16_t S, WR_BYTE wrByteFunc))
F(5, uint16_t, sendPacketStart, (wrByteFunc), (WR_BYTE wrByteFunc))
F_VOID(6, sendPacketEnd, (S, wrByteFunc), (uint16_t S, WR_BYTE wrByteFunc))
F_VOID(7, sendOk, (cmd, wrByteFunc), (uint8_t cmd, WR_BYTE wrByteFunc))
F_VOID(8, sendError, (cmd, err, wrByteFunc), (uint8_t cmd, uint8_t err, WR_BYTE wrByteFunc))
F(9, uint16_t, readADC, (adc_input, vref_type), (uint8_t adc_input, uint8_t vref_type))

#endif /* BL_H_ */
