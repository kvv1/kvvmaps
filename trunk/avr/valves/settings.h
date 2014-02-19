#ifndef __SETTINGS_H__
#define __SETTINGS_H__

#include "board2.h"

#define BINARY_DATA           
//#define MY_ADDR 31
#define MY_ADDR 26

#define TX_START() setPort(PIN_485, 1)
#define TX_END() setPort(PIN_485, 0)

#endif
