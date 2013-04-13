#ifndef __SETTINGS_H__
#define __SETTINGS_H__

#include "board2.h"

#define TRACE

#define TX_START() setPort(PIN_485, 1)
#define TX_END() setPort(PIN_485, 0)
 
#define vmStep(ms)

#endif
