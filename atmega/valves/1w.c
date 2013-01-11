#include "1wire.h"
#include "1w.h"
           
unsigned char _w1_select(unsigned char *addr) {
    unsigned char i;
    if (_w1_init()==0) 
        return 0;
    if (addr) {
        _w1_write(0x55);
        i=0;
        do {
            _w1_write(*(addr++));
        } while (++i<8);
    } else { 
        _w1_write(0xcc); 
    }
    return 1;
}

void _w1_write(char c) {
   #asm ("cli");
   w1_write(c);
   #asm ("sei");
}          

char _w1_read() {
    char c;
   #asm ("cli");
   c = w1_read();
   #asm ("sei");
   return c;
}
         
char _w1_init() {
    char c;
   #asm ("cli");
   c = w1_init();
   #asm ("sei");
   return c;
}
            
unsigned char _w1_search(unsigned char cmd,void *p) {
    return w1_search(cmd, p);
}

unsigned char _w1_dow_crc8(void *p,unsigned char n) {
    return w1_dow_crc8(p, n);
}


