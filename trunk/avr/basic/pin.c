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

