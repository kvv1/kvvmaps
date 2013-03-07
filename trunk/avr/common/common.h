#ifndef __COMMON__
#define __COMMON__

#include <avr/io.h>

#include "settings.h"

#ifndef NTIMERS
#define NTIMERS 16
#endif

#include "object.h"
#include "timer0.h"
#include "message.h"
#include "pin.h"
#include "led.h"
#include "adc.h"
#include "myio.h"

#ifndef ADC_CHANS
#define ADC_CHANS 0
#endif

void foo();

void addFletchSum(unsigned char c, unsigned char* S);
unsigned char fletchSum(unsigned char *buf, unsigned char len);

void createObjects();

#endif

