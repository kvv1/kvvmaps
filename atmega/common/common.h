#ifndef __COMMON__
#define __COMMON__

#define CHARGER_ADDR 1

#include "settings.h"


#define MSG_WI_CHARGER_GET_CURRENT 100
#define MSG_WI_CHARGER_GET_VOLTAGE 101




#ifdef ATMEGA48
#include <mega48.h>

#ifndef FOSC
#define FOSC 8000000L // Clock Speed
#endif
#define MSG_QUEUE_SIZE 8
#define NTIMERS 6

#else

#ifdef ATMEGA8
#include <mega8.h>

#ifndef FOSC
#define FOSC 8000000L // Clock Speed
#endif
#define MSG_QUEUE_SIZE 16
#define NTIMERS 16


#else

#ifdef ATMEGA168
#include <mega168.h>

#ifndef FOSC
#define FOSC 8000000L // Clock Speed
#endif
#define MSG_QUEUE_SIZE 32
#define NTIMERS 16

#else
#error

#endif
#endif
#endif

#include "object.h"
#include "timer0.h"
#include "message.h"
#include "pin.h"
#include "led.h"
#include "adc.h"

#include <stdlib.h>
#include <math.h>

void putchar(char c);

#pragma used+
void print2(flash char* format, int n1, int n2);
void print1(flash char* format, int n1);
void print0(flash char* format);
#pragma used-

#ifdef TRACE
#define trace0(x) print0(x)
#define trace1(x,y) print1(x,y)
#define trace2(x,y,z) print2(x,y,z)
#else
#define trace0(x) do{}while(0)
#define trace1(x,y) do{}while(0)
#define trace2(x,y,z) do{}while(0)
#endif              

#ifndef ADC_CHANS
#define ADC_CHANS 0
#endif


//#define print1(format, n1) print2(format, n1, 0)
//#define print0(format) print2(format, 0, 0)




void foo();
extern void (*idle)();

typedef unsigned char byte;
#pragma used+
void addFletchSum(unsigned char c, unsigned char* S);
unsigned char fletchSum(unsigned char *buf, unsigned char len);
#pragma used-


#define TWIE 0
#define TWEN 2
#define TWWC 3
#define TWSTO 4
#define TWSTA 5
#define TWEA 6
#define TWINT 7



#endif

