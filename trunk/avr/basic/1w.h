#ifndef OW_H_
#define OW_H_

unsigned char oneWireInit();
void oneWireWriteByte(unsigned char data);
unsigned char oneWireReadByte(void);
//void startConvert();
int getTemperature();

extern int temperature;
void ds18b20_step(int ms);

#endif /* OW_H_ */
