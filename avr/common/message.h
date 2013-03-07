#ifndef __MESSAGE__
#define __MESSAGE__

#include "pin.h"

struct _ObjectHeader;

typedef struct {
    struct _ObjectHeader* target;
    char cmd;
    int param1;
    int param2;
} Message;

int sendMessage(struct _ObjectHeader* target, char cmd, int param1, int param2);
void postMessage(struct _ObjectHeader* target, char cmd, int param1, int param2);
char getMessage(Message* msg);
                 
#define MSG_INTERNAL 128

void setPinListener(PORTPIN portPin, struct _ObjectHeader* obj, char cmd);
void handlePins();


#endif
