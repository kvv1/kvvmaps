#ifndef __OBJECT__
#define __OBJECT__

#include "message.h"

struct _ObjectHeader;
typedef int (*handlerType) (Message* msg);

typedef struct _ObjectHeader {
    handlerType handler;
} ObjectHeader;

#endif
