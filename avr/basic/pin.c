#include <avr/io.h>
#include "pin.h"

void setPortBit1(PORTPIN portpin, char offset, char b) {
	int _portpin = (unsigned int) portpin;
	volatile uint8_t* addr = (volatile uint8_t*) ((_portpin >> 8) + offset);
	char bits = _portpin & 0xFF;
	if (b)
		*addr |= bits;
	else
		*addr &= ~bits;
}


/*
 void setPortBit(unsigned int portpin, char offset, char b) {
 volatile uint8_t* addr = (volatile uint8_t*) ((portpin >> 8) + offset);
 char bits = portpin & 0xFF;
 if (b)
 *addr |= bits;
 else
 *addr &= ~bits;
 }

 char getPortBit(unsigned int portpin, char offset) {
 volatile uint8_t* addr = (volatile uint8_t*) ((portpin >> 8) + offset);
 char bits = portpin & 0xFF;
 return ((*addr) & bits) != 0;
 }
 */

