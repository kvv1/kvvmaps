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

typedef struct {
	uint8_t addr;
	uint8_t func;
	uint8_t data[];
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

#endif /* BL_H_ */
