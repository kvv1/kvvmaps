#ifndef __SETTINGS_H__
#define __SETTINGS_H__

#define ATMEGA8
#define AVCC_REF

#include "board2.h"

#define FOSC 8200000L // Clock Speed

enum {
    MSG_INIT_MARS,
    MSG_CMD,
    MSG_TIMER_WORK,
    MSG_TIMER_STOP
};

#define BINARY_DATA           
#define MY_ADDR 29      

#define TX_START() setPort(PIN_485, 1)
#define TX_END() setPort(PIN_485, 0)
            

#define PERIOD 10000
#define PULSE 3000

 
#endif
