#include <avr/io.h>
#include <avr/interrupt.h>
#include <string.h>
#include <util/delay.h>
#include "utils.h"
#include "hw.h"
#include "packet.h"

//#define MAGIC8 0x5A
//#define MAGIC16 0xE6C9

//static int magic16;

void __jumpMain(void) __attribute__ ((naked)) __attribute__ ((section (".init9")));
void __jumpMain(void) {
	asm volatile ( "rjmp main");
	asm volatile ( "rjmp getAddr");
	asm volatile ( "rjmp crc16_step");
	asm volatile ( "rjmp crc16");
}

uint8_t getAddr() {
	return MYADDR;
}

Globals globals;

//void on() {
//	DDRD |= 1 << 2;
//	PORTD |= 1 << 2;
//}
//
//void off() {
//	DDRD |= 1 << 2;
//	PORTD &= ~(1 << 2);
//}

register uint8_t reg_r1 asm("r1");
#define init() do { SP = RAMEND; reg_r1 = 0; SREG = reg_r1; } while(0)

int main() {
	cli();
	init();

//	on();

	memset(&globals, 0, sizeof(globals));
	globals.lastPage = 0xFFFF;

	hwInit();

	while (globals.startCnt < (unsigned int) (START_TIMEOUT_US / WAIT_UNIT_US)
			|| !isAppOK()) {

		int b = rdByte();
		if (b == -1) {
			if (globals.inputIdx) {
				globals.magic16 = MAGIC16;
				packetReceived(globals.inputBuffer, globals.inputIdx);
				globals.magic16 = 0;
			}
			globals.inputIdx = 0;

		} else {
			if (globals.inputIdx < BOOT_INPUT_BUFFER_SIZE)
				globals.inputBuffer[globals.inputIdx++] = b;
		}
	}

//	off();

#if defined(__AVR_ATmega168__)
	asm volatile ( "jmp 0");

#define BOOTSIZE 512 // in words
#else
#ifdef __AVR_ATmega8__

	asm volatile ( "rjmp 0");

#else
#error
#endif
#endif
}
