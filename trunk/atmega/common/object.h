#ifndef __OBJECT__
#define __OBJECT__

#include "message.h"

struct _ObjectHeader;
typedef int (*handlerType) (struct _ObjectHeader* this, Message* msg);

typedef struct _ObjectHeader {
    handlerType handler;
} ObjectHeader;

#define MSG_RX 254

#endif
