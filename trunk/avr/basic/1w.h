#ifndef OW_H_
#define OW_H_

unsigned char oneWireInit(char n);
void oneWireWriteByte(char n, unsigned char data);
unsigned char oneWireReadByte(char n);
//void startConvert();
int oneWireGetTemperature(char n);
#define TEMPERATURE_INVALID (-9999)

extern int temperature[2];
void ds18b20_step(int n, int ms);

#endif /* OW_H_ */
