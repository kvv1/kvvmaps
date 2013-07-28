#include <avr/io.h>
#include <stdlib.h>
#include <util/atomic.h>

#include "object.h"
#include "message.h"
#include "myio.h"
#include "pin.h"

#ifdef __AVR_ATmega8__
#define MSG_QUEUE_SIZE 8
#else
#ifdef __AVR_ATmega48__
#define MSG_QUEUE_SIZE 16
#else
#ifdef __AVR_ATmega168__
#define MSG_QUEUE_SIZE 32
#else
#error
#endif
#endif
#endif

static Message msgQueue[MSG_QUEUE_SIZE];
static char msgCnt;
static int msgWrIdx;
static int msgRdIdx;

int sendMessage(struct _ObjectHeader* target, char cmd, int param1, int param2) {
	Message msg;
	msg.cmd = cmd;
	msg.target = target;
	msg.param1 = param1;
	msg.param2 = param2;
	return target->handler(&msg);
}

void postMessage(struct _ObjectHeader* target, char cmd, int param1, int param2) {
	char msgOvf = 0;
	ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {
		if (msgCnt == MSG_QUEUE_SIZE) {
			msgOvf = 1;
		} else {
			Message* msg = &msgQueue[msgWrIdx];
			msg->cmd = cmd;
			msg->target = target;
			msg->param1 = param1;
			msg->param2 = param2;
			if (++msgWrIdx == MSG_QUEUE_SIZE)
				msgWrIdx = 0;
			++msgCnt;
		}
	}

	if (msgOvf)
		trace1("msg buf ovf %d\r\n", cmd);
}

char getMessage(Message* msg) {
	if (msgCnt == 0)
		return 0;

	*msg = msgQueue[msgRdIdx];
	if (++msgRdIdx == MSG_QUEUE_SIZE)
		msgRdIdx = 0;

	ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {
		msgCnt--;
	}

	return 1;
}

void handleMessages() {
	Message msg;
	if (getMessage(&msg)) {
		if (msg.target)
			msg.target->handler(&msg);
	}
}


/*
 void removeMessage(char cmd, struct _ObjectHeader* target) {
 char sreg_val = qcli();
 char i = msgRdIdx;
 char cnt = msgCnt;
 while(cnt--) {
 Message* msg;
 if(++i == MSG_QUEUE_SIZE)
 i = 0;
 msg = &msgQueue[i];
 if(msg->cmd == cmd && msg->target == target)
 msg->target = 0;
 }
 qsei(sreg_val);
 }
 */

#define NPINS 4

typedef struct {
	ObjectHeader* obj;
	char cmd;
	PORTPIN portPin;
	char state;
} PIN;

static PIN pins[NPINS];

void handlePins() {
	int i;
	for (i = 0; i < NPINS; i++) {
		PIN* pin = &pins[i];
		if (pin->obj) {
			char st = getPin(pin->portPin);
			if (st != pin->state) {
				pin->state = st;
				sendMessage(pin->obj, pin->cmd, st, 0);
			}
		}
	}
}

void setPinListener(PORTPIN portPin, ObjectHeader* obj, char cmd) {

	int i;
	PIN* freePin = 0;

	if (portPin == NO_PIN)
		return;

	for (i = 0; i < NPINS; i++) {
		PIN* pin = &pins[i];
		if (pin->portPin == portPin) {
			freePin = pin;
			break;
		} else if (!pin->obj) {
			freePin = pin;
		}
	}
	if (freePin) {
		freePin->cmd = cmd;
		freePin->obj = obj;
		freePin->portPin = portPin;
		freePin->state = getPin(portPin);
	} else {
		trace0("no free pins\r\n");
	}
}
