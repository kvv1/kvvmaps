#ifndef __PIN__
#define __PIN__

#include <stdint.h>

typedef struct {
}*PINDIR;

#define PIN_OUT ((PINDIR)1)
#define PIN_IN ((PINDIR)0)

typedef struct {
}*PORTPIN;

#define PORT_BIT(pin, bit) ((PORTPIN)((((int)(&pin) << 8) | (1<<bit))))

#ifdef ATMEGA32
#define P_A0 PORT_BIT(PINA, 0)
#define P_A1 PORT_BIT(PINA, 1)
#define P_A2 PORT_BIT(PINA, 2)
#define P_A3 PORT_BIT(PINA, 3)
#define P_A4 PORT_BIT(PINA, 4)
#define P_A5 PORT_BIT(PINA, 5)
#define P_A6 PORT_BIT(PINA, 6)
#define P_A7 PORT_BIT(PINA, 7)
#endif

#define P_B0 PORT_BIT(PINB, 0)
#define P_B1 PORT_BIT(PINB, 1)
#define P_B2 PORT_BIT(PINB, 2)
#define P_B3 PORT_BIT(PINB, 3)
#define P_B4 PORT_BIT(PINB, 4)
#define P_B5 PORT_BIT(PINB, 5)
#define P_B6 PORT_BIT(PINB, 6)
#define P_B7 PORT_BIT(PINB, 7)

#define P_C0 PORT_BIT(PINC, 0)
#define P_C1 PORT_BIT(PINC, 1)
#define P_C2 PORT_BIT(PINC, 2)
#define P_C3 PORT_BIT(PINC, 3)
#define P_C4 PORT_BIT(PINC, 4)
#define P_C5 PORT_BIT(PINC, 5)
#define P_C6 PORT_BIT(PINC, 6)
#define P_C7 PORT_BIT(PINC, 7)

#define P_D0 PORT_BIT(PIND, 0)
#define P_D1 PORT_BIT(PIND, 1)
#define P_D2 PORT_BIT(PIND, 2)
#define P_D3 PORT_BIT(PIND, 3)
#define P_D4 PORT_BIT(PIND, 4)
#define P_D5 PORT_BIT(PIND, 5)
#define P_D6 PORT_BIT(PIND, 6)
#define P_D7 PORT_BIT(PIND, 7)

#define NO_PIN ((PORTPIN)0)

inline void setPortBit(PORTPIN portpin, char offset, char b) {
	int _portpin = (unsigned int) portpin;
	volatile uint8_t* addr = (volatile uint8_t*) ((_portpin >> 8) + offset);
	char bits = _portpin & 0xFF;
	if (b)
		*addr |= bits;
	else
		*addr &= ~bits;
}

inline void setPortBit_0(PORTPIN portpin, char offset) {
	int _portpin = (unsigned int) portpin;
	volatile uint8_t* addr = (volatile uint8_t*) ((_portpin >> 8) + offset);
	char bits = _portpin & 0xFF;
	*addr &= ~bits;
}

inline void setPortBit_1(PORTPIN portpin, char offset) {
	int _portpin = (unsigned int) portpin;
	volatile uint8_t* addr = (volatile uint8_t*) ((_portpin >> 8) + offset);
	char bits = _portpin & 0xFF;
	*addr |= bits;
}

inline char getPortBit(PORTPIN portpin, char offset) {
	int _portpin = (unsigned int) portpin;
	volatile uint8_t* addr = (volatile uint8_t*) ((_portpin >> 8) + offset);
	char bits = _portpin & 0xFF;
	return ((*addr) & bits) != 0;
}

//void setPortBit(PORTPIN portpin, char offset, char b);
//#define setPort(portPin, val) setPortBit1(portPin, 2, val)

#define setPort(portPin, val) setPortBit(portPin, 2, val)
#define setDDR(portPin, val) setPortBit(portPin, 1, val)

inline void confPin(PORTPIN portPin, PINDIR dir, char pullUp) {
	setDDR(portPin, (int)dir);
	setPort(portPin, pullUp);
}

#define getPin(portPin) getPortBit(portPin, 0)
#define getPort(portPin) getPortBit(portPin, 2)

#endif
