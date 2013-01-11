#ifndef __MESSAGE__
#define __MESSAGE__

struct _ObjectHeader;

typedef struct {
    char cmd;
    struct _ObjectHeader* target;
    int data;
} Message;

#pragma used+
int sendMessage(char cmd, struct _ObjectHeader* target, int data);
void postMessage(char cmd, struct _ObjectHeader* target, int data);
int sendMessage0(char cmd, struct _ObjectHeader* target);
void postMessage0(char cmd, struct _ObjectHeader* target);
#pragma used-
char hasMessage();
void getMessage(Message* msg);
                 
#define MSG_INTERNAL 128

#endif