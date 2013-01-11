#ifndef __PIN__
#define __PIN__

#include "object.h"

#define NO_PIN 0

#define PB 0x00
#define PC 0x10
#define PD 0x20

#ifdef ATMEGA32
#define PA 0x30

#define PA0 PA
#define PA1 (PA | 1)
#define PA2 (PA | 2)
#define PA3 (PA | 3)
#define PA4 (PA | 4)
#define PA5 (PA | 5)
#define PA6 (PA | 6)
#define PA7 (PA | 7)
#endif

#define PB0 PB
#define PB1 (PB | 1)
#define PB2 (PB | 2)
#define PB3 (PB | 3)
#define PB4 (PB | 4)
#define PB5 (PB | 5)
#define PB6 (PB | 6)
#define PB7 (PB | 7)

#define PC0 PC
#define PC1 (PC | 1)
#define PC2 (PC | 2)
#define PC3 (PC | 3)
#define PC4 (PC | 4)
#define PC5 (PC | 5)
#define PC6 (PC | 6)
#define PC7 (PC | 7)

#define PD0 PD
#define PD1 (PD | 1)
#define PD2 (PD | 2)
#define PD3 (PD | 3)
#define PD4 (PD | 4)
#define PD5 (PD | 5)
#define PD6 (PD | 6)
#define PD7 (PD | 7)

#define PIN_IN 0
#define PIN_OUT 1

#pragma used+
void confPin(char portPin, char dir, char pullUp);
void setPort(char portPin, char val);
char getPort(char portPin);
char getPin(char portPin);
void setPinListener(char portPin, ObjectHeader* obj, char cmd); 

void checkPins();
#pragma used-


#endif
