#ifndef OW_H_
#define OW_H_

unsigned char oneWireInit(char n);
void oneWireWriteByte(char n, unsigned char data);
unsigned char oneWireReadByte(char n);
//void startConvert();
int oneWireGetTemperature(char n);
#define TEMPERATURE_INVALID (-9999)

void w1Init();
int w1_temp(uint8_t n);
void ds18b20_step(uint8_t n, int ms);

void W1_OFF(uint8_t n);

#endif /* OW_H_ */
