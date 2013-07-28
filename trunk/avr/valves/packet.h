void packetReceived(char* data, uint8_t len);
void sendPacket(uint8_t* data, uint16_t len);

uint16_t sendPacketStart();
uint16_t sendPacketBodyPart(uint8_t* data, uint16_t len, uint16_t S);
void sendPacketEnd(uint16_t S);
uint16_t sendByte(uint8_t b, uint16_t S);
uint16_t sendWord(uint16_t w, uint16_t S);

int fetch(void* a);
