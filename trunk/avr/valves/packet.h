void packetReceived(char* data, uint8_t len);
void sendPacket(char* data, uint16_t len);

void sendPacketStart(uint16_t len, uint8_t* S);
void sendPacketBodyPart(uint8_t* data, uint16_t len, uint8_t* S);
void sendPacketEnd(uint8_t* S);
void sendByte(uint8_t b, uint8_t* S);
