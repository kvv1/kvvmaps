void _w1_write(char c);
char _w1_read();
char _w1_init();
unsigned char _w1_select(unsigned char *addr);
unsigned char _w1_search(unsigned char cmd,void *p);
unsigned char _w1_dow_crc8(void *p,unsigned char n);
