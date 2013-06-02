#ifndef __SETTINGS_H__
#define __SETTINGS_H__

#include "board2.h"

enum {
	MSG_INIT_MARS, MSG_CMD,
};

#define BINARY_DATA           
#define MY_ADDR 20

#define TX_START() setPort(PIN_485, 1)
#define TX_END() setPort(PIN_485, 0)

#endif
