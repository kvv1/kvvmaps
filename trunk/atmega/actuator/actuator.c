#include "common.h"

#define ACTUATOR_SENSOR_TIMEOUT 500
              
enum {
    MSG_ACTUATOR_INIT = MSG_INTERNAL,
    MSG_ACTUATOR_SENSOR,
    MSG_ACTUATOR_SENSOR_DOWN,
    MSG_ACTUATOR_SENSOR_UP,
    MSG_ACTUATOR_TIMER,
    MSG_ACTUATOR_TIMER_DOWN,
    MSG_ACTUATOR_TIMER_UP,

    MSG_ACTUATOR_TEST,
    
    MSG_ACTUATOR_GOTO
};

typedef struct {
    ObjectHeader h;
    char pinOut0;
    char pinOut1;
    char pinIn;
    int pos;
    int target;
    int range;
    int inc;
} Actuator;
              
static void stop(Actuator* this) {
    this->inc = 0;
    setPort(this->pinOut0, 0);
    setPort(this->pinOut1, 0);
    killTimer(this, MSG_ACTUATOR_TIMER);
    killTimer(this, MSG_ACTUATOR_TIMER_DOWN);
    killTimer(this, MSG_ACTUATOR_TIMER_UP);
}

static void up(Actuator* this) {
    setPort(this->pinOut0, 0);
    setPort(this->pinOut1, 1);
}

static void down(Actuator* this) {
    setPort(this->pinOut0, 1);
    setPort(this->pinOut1, 0);
}

#pragma warn-
static int handler(Actuator* this, Message* msg) {
    switch(msg->cmd) {
    case MSG_ACTUATOR_INIT:
        trace0("init\r\n");
        stop(this);
        setPinListener(this->pinIn, this, MSG_ACTUATOR_SENSOR_DOWN);
        setTimer(this, MSG_ACTUATOR_TIMER_DOWN, TIMER_NORMAL, ACTUATOR_SENSOR_TIMEOUT);
        down(this);
        break;
    case MSG_ACTUATOR_SENSOR_DOWN: 
        trace0("s_d\r\n");
        setTimer(this, MSG_ACTUATOR_TIMER_DOWN, TIMER_NORMAL, ACTUATOR_SENSOR_TIMEOUT);
        break;
    case MSG_ACTUATOR_TIMER_DOWN:
        trace0("t_d\r\n");
        stop(this);
        this->pos = 0;
        setPinListener(this->pinIn, this, MSG_ACTUATOR_SENSOR_UP);
        setTimer(this, MSG_ACTUATOR_TIMER_UP, TIMER_NORMAL, ACTUATOR_SENSOR_TIMEOUT);
        up(this);
        break;
    case MSG_ACTUATOR_SENSOR_UP: 
        trace0("s_u\r\n");
        setTimer(this, MSG_ACTUATOR_TIMER_UP, TIMER_NORMAL, ACTUATOR_SENSOR_TIMEOUT);
        this->pos++;
        break;
    case MSG_ACTUATOR_TIMER_UP:
        trace0("t_u\r\n");
        stop(this);
        setPinListener(this->pinIn, this, MSG_ACTUATOR_SENSOR);
        this->range = this->pos;
        
        postMessage(MSG_ACTUATOR_GOTO, this, 128);
        
        break;
    case MSG_ACTUATOR_GOTO:
        trace1("gt %d\r\n", msg->data);
        
        if(!this->range)
            break;
        
        stop(this);
        this->target = (int)((long)msg->data * (long)this->range >> 8);
        trace1("pos %d\r\n", this->pos);
        trace1("range %d\r\n", this->range);
        trace1("targ %d\r\n", this->target);
        //this->target = 10;        
        if(this->target > this->pos) {
            trace0("u\r\n");
            this->inc = 1;
            setTimer(this, MSG_ACTUATOR_TIMER, TIMER_NORMAL, ACTUATOR_SENSOR_TIMEOUT);
            up(this);
        } else if(this->target < this->pos) {
            trace0("d\r\n");
            this->inc = -1;
            setTimer(this, MSG_ACTUATOR_TIMER, TIMER_NORMAL, ACTUATOR_SENSOR_TIMEOUT);
            down(this);
        }
        break;
    case MSG_ACTUATOR_SENSOR:
        trace0("s\r\n");
        this->pos += this->inc;
        setTimer(this, MSG_ACTUATOR_TIMER, TIMER_NORMAL, ACTUATOR_SENSOR_TIMEOUT);    
        if(this->pos == this->target)
            stop(this);
        break;
    case MSG_ACTUATOR_TIMER:
        trace0("t\r\n");
        stop(this);
        break;
/*        
    case MSG_ACTUATOR_TEST:
        setPort(this->pinOut1, !getPin(this->pinOut1));
        setTimer(this, MSG_ACTUATOR_TEST, TIMER_NORMAL, 50);
        break;
*/
    }         
}       

static Actuator obj = {{handler}, OUT0, OUT1, IN0} ;

void createObjects() {  
    trace0("createObjects\r\n");
    confPin(OUT0, PIN_OUT, 0);
    confPin(OUT1, PIN_OUT, 0);
    confPin(IN0, PIN_IN, 1);
    
    postMessage0(MSG_ACTUATOR_INIT, &obj);
//    postMessage0(MSG_ACTUATOR_TEST, &obj);
}

void handleRxCmd(char* cmd) {}
