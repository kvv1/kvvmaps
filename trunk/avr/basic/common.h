#ifndef __COMMON__
#define __COMMON__

#include <avr/io.h>

#include "settings.h"

#ifndef NTIMERS
#define NTIMERS 16
#endif

//#include "object.h"
#include "timer0.h"
//#include "message.h"
#include "pin.h"
//#include "led.h"
#include "adc.h"
#include "myio.h"
#include "1w.h"
#include "ee.h"

#ifndef ADC_CHANS
#define ADC_CHANS 0
#endif

void foo();

int commonMain(void);

//void addFletchSum(uint8_t c, uint8_t* S);
//unsigned char fletchSum(uint8_t *buf, uint8_t len);

#define CRC16_INIT 0xffff
uint16_t crc16_step(uint8_t c, uint16_t crc_val);
uint16_t crc16(uint8_t * buf, int nbytes);



void createObjects();

#endif

