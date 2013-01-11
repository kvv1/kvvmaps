#include "common.h"
#include "timer0.h"
#include "message.h"
#include "adc.h"
#include "keyboard.h"
                


#define _MSG_KEYBOARD_TIMER_PERIOD 100
                     
enum {
    _MSG_KEYBOARD_TIMER = MSG_INTERNAL
};

#define MAX_KEYS 10

static struct {
    ObjectHeader* obj;
    char cmd;
} keys [MAX_KEYS];

static int handler(ObjectHeader* this, Message* msg);
static ObjectHeader keysObj = {handler} ;

void setKeyListener(char key, ObjectHeader* obj, char cmd) {
    static char initialized;
    if(!initialized) {
        initialized = 1;
        setTimer(&keysObj, _MSG_KEYBOARD_TIMER, TIMER_PERIODIC, _MSG_KEYBOARD_TIMER_PERIOD);
    }

    if(key >= MAX_KEYS)
        return;

    keys[key].obj = obj;
    keys[key].cmd = cmd;
}

static flash int keyVals[] = {
    100,
    300,
    450,
    550,
    600,
    670,
    1000
};

#pragma used+
static int handler(ObjectHeader* this, Message* msg) {
    static char prevKey = 255;
    static int time;
    unsigned int val = read_adc(KEYBOARD_CHAN);
    char key = 255;
    char i;

    for(i = 0; i < sizeof(keyVals)/sizeof(keyVals[0]); i++) {
        if(val < keyVals[i]) {
            key = i;
            break;
        }
    }        
    
    if(key != prevKey) {
        trace2("key=%d val=%d\r\n", key, val);
        if(prevKey != 255 && keys[prevKey].obj) {
            if(time >= 0)
                postMessage(keys[prevKey].cmd, keys[prevKey].obj, 0);
            time = -1;
        }
        if(key != 255 && keys[key].obj) {
            postMessage(keys[key].cmd, keys[key].obj, 1);
            time = 0;
        }
        prevKey = key;
    } else if(key != 255 && time != -1) {
        if(time < 1000) {
            time += _MSG_KEYBOARD_TIMER_PERIOD;
        } else if(keys[prevKey].obj) {
            postMessage(keys[key].cmd, keys[key].obj, 2);
            time = -1;
        }
    }

    return 0;
}
#pragma used-
