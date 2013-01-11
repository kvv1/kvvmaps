#include "common.h"
#include "packet.h"

static char addr;

void packetReceived(char* data, char len);
                                          
void handleRxCmd(char* data) {
    if(data[1] == 0 || data[1] == MY_ADDR) {
        char len = data[0];
        addr = data[1];
        if(fletchSum(data, len - 1) == data[len - 1])
            packetReceived(data + 2, len - 3);
    }
}

void sendPacket(char* data, char len) {
    unsigned char S = 0;
    
    putchar(len + 3);
    addFletchSum(len + 3, &S);
    
    putchar(addr | 0x80);
    addFletchSum(addr | 0x80, &S);
    
    while(len--) {
        putchar(*data);
        addFletchSum(*data, &S);
        data++;        
    }
    
    putchar(S);
}

