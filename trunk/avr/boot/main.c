#include <avr/io.h>
#include <avr/interrupt.h>
#include <string.h>
#include "utils.h"
#include "hw.h"
#include "packet.h"
#include "address.h"

//#define MAGIC8 0x5A
//#define MAGIC16 0xE6C9

//static int magic16;

void __jumpMain(void) __attribute__ ((naked)) __attribute__ ((section (".init9")));
void __jumpMain(void) {
	asm volatile ( "rjmp main");
	asm volatile ( "rjmp checkPacket");
	asm volatile ( "rjmp getAddr");
}

uint8_t getAddr() {
	return MYADDR;
}

Globals globals;

register uint8_t reg_r1 asm("r1");
#define init() do { SP = RAMEND; reg_r1 = 0; SREG = reg_r1; } while(0)

void initGlobals() {
	memset(&globals, 0, sizeof(globals));
	globals.jump_to_app = 0;
	globals.lastPage = 0xFFFF;
	initHW();
}

int main() {
	cli();
	init();
	initGlobals();
	while (startCnt < (unsigned int) (START_TIMEOUT_US / WAIT_UNIT_US)
			|| !isAppOK()) {

		int b = rdByte();
		if (b == -1) {
			if (globals.inputIdx) {
				packetReceived(globals.inputBuffer, globals.inputIdx);
			}
			globals.inputIdx = 0;

		} else {
			if (globals.inputIdx < BOOT_INPUT_BUFFER_SIZE)
				globals.inputBuffer[globals.inputIdx++] = b;
		}
	}
	globals.jump_to_app();
}

/*
 void pageWrite(int page, unsigned char magic8) {
 asm volatile("mov R30,%A0" "\n\t"
 "mov R31,%B0" :"=r" (page));
 if (magic16 != MAGIC16)
 return;
 SPMCR = ((1 << SPMEN) | (1 << PGWRT));
 if (magic8 == MAGIC8)
 asm volatile ( "spm");
 }
 void _pageWrite(int page, unsigned char magic8) {
 boot_page_write(page);
 }
 */
