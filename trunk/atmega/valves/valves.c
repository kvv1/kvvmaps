#include "common.h"
#include "packet.h"
#include "commands.h"
              
static char* cmd;
static char cmdlen;
             
#include "1w.h"
        
#include "ds18b20.h"
             
#define MAX_DEVICES 8
static unsigned char rom_code[MAX_DEVICES][9];

int getTemp10() {
    float t = ds18b20_temperature(&rom_code[0][0]);
    if(t == -9999)
        return TEMP_INVALID;
    return (int) (10 * t);
}


#define MARS_COMMANDS_NUM 6
static flash char* flash marsCommands[] = {"00#RS2","00#I96","00#E96","00#F10","00#D4321","00#AFFFF" };
static eeprom char marsCommandsCnt = -1;

#pragma warn-
static int handler(ObjectHeader* this, Message* msg) {
    switch(msg->cmd) {
    case MSG_INIT_MARS: 
        {
            flash char* p = marsCommands[marsCommandsCnt];
            while(*p) {
                putchar(*(p++));
            }
            putchar(3);
            marsCommandsCnt++;
            if(marsCommandsCnt != MARS_COMMANDS_NUM)
                setTimer(this, MSG_INIT_MARS, TIMER_NORMAL, 0x2000);
            else {
                //confPin(PIN_MARS_RTS, PIN_OUT, 0);
                //setPort(PIN_MARS_RTS, 0);
            }
        }
        break;
    case MSG_CMD:
        handleCmd(cmd, cmdlen);
        break;
    case MSG_TIMER_WORK:
        {
            int t = getTemp10();
            if(t != TEMP_INVALID) {
                if(t < temp_pref_ee) {
                    setPort(OUT0, 1);
                    setTimer(this, MSG_TIMER_STOP, TIMER_NORMAL, PULSE);
                }
                if(t > temp_pref_ee) {
                    setPort(OUT1, 1);
                    setTimer(this, MSG_TIMER_STOP, TIMER_NORMAL, PULSE);
                }
                if(t > temp_pref_ee_2) {
                    setPort(OUT2, 1);
                }
                if(t <= temp_pref_ee) {
                    setPort(OUT2, 0);
                }
            }
        }
        break;
    case MSG_TIMER_STOP:
            setPort(OUT0, 0);
            setPort(OUT1, 0);
        break;
    }
}
              
static ObjectHeader obj = {handler} ;

void confMars() {
//    if(marsCommandsCnt != MARS_COMMANDS_NUM) {
//        if(marsCommandsCnt == -1)
            marsCommandsCnt = 0;
        //setPort(PIN_MARS_RTS, 1);
//        postMessage(MSG_INIT_MARS, &obj, 0);
//    }
}


void createObjects() {  
    _w1_search(0xf0, rom_code);
    ds18b20_init(&rom_code[0][0], 0, 50, DS18B20_9BIT_RES);
    
    initCommands();
    confPin(PIN_485, PIN_OUT, 0);
    confPin(OUT0, PIN_OUT, 0);
    confPin(OUT1, PIN_OUT, 0);
    confPin(OUT2, PIN_OUT, 0);
    confPin(OUT3, PIN_OUT, 0);

    confMars();                       
    startStopTempReg();
}
                         
void packetReceived(char* data, char len) {
    cmd = data;
    cmdlen = len;
    postMessage(MSG_CMD, &obj, 0);
}

void startStopTempReg() {
    if(temp_pref_on_ee) {    
        postMessage(MSG_TIMER_WORK, &obj, 0);
        setTimer(&obj, MSG_TIMER_WORK, TIMER_PERIODIC, PERIOD);
    } else {
        killTimer(&obj, MSG_TIMER_WORK);
        killTimer(&obj, MSG_TIMER_STOP);
        postMessage(MSG_TIMER_STOP, &obj, 0);
    }
}
