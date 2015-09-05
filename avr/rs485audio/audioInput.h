#ifndef AUDIOINPUT_H_
#define AUDIOINPUT_H_

void byteReceived(uint8_t b);
void endOfInput();
void transitionReceived(uint8_t cnt);

#endif /* AUDIOINPUT_H_ */
