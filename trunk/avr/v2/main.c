#include <avr/io.h>
#include <avr/interrupt.h>
#include <stdint.h>
#include <util/delay.h>
#include <avr/wdt.h>
#include "hw.h"
#include "io.h"
#include "bl.h"
#include "adc.h"
#include "1w.h"
#include "ee.h"
#include "pwm.h"
#include "inputs.h"
#include "packet.h"
#include "commands.h"
#include "rules.h"
#include "regs.h"

#define INPUT_BUFFER_SIZE 64

#define VERSION 6

void packetReceived(uint8_t* buffer, uint8_t len);

int main() {
	int mcucsr = MCUCSR;
	MCUCSR = 0;

//	ee_magic = MAGIC16;
//	if (mcucsr & (1 << PORF))
//			{
//	}
//	else if (mcucsr & (1 << BORF))
//		setbodCnt(getbodCnt() + 1);
//	else if (mcucsr & (1 << WDRF))
//		setwdtCnt(getwdtCnt() + 1);
//	ee_magic = 0;

	hwInit(1);
	timer0Init();
	adcInit(0);
	w1Init();
	initPWM();
	inputsInit();

	wdt_enable(WDTO_2S);

	sei();

	ee_magic = MAGIC16;
//	if (getCodeLen() == 0xFFFF) {
//		setCodeLen(0);
//		setCodeCRC(CRC16_INIT);
//	}

//initCommands();
//initVM();
	ee_magic = 0;

	while (1) {

		char tticks = getClearTimerTicks();
		while (tticks) {
			tticks--;
			ds18b20_step(0, TIME_UNIT);
			ds18b20_step(1, TIME_UNIT);
			//vmStep(TIME_UNIT);
			handlePWM(TIME_UNIT);
			stepRules();
		}

//		if(!transmitting())
//			wdt_reset();

		uint8_t* buf;
		uint8_t bufSz;
		buf = getPacket(&bufSz);
		if (buf) {
			ee_magic = MAGIC16;
			packetReceived(buf, bufSz);
			ee_magic = 0;
			startReceiving();
			wdt_reset();
		}
	}
}

void packetReceived(uint8_t* buffer, uint8_t len) { // returns consumed flag
	ADU* adu = (ADU*) (buffer);

	if (len < 3)
		return;

	uint8_t addr = adu->addr;
	if (addr != bl_getAddr() && addr != 0)
		return;

	uint16_t sum = bl_crc16(buffer, len - 2);

	if ((buffer[len - 2] != (char) sum)
			|| (buffer[len - 1] != (char) (sum >> 8)))
		return;

	switch (adu->pdu.func) {
	case 100:
		sendOk(adu->pdu.func);
		waitTransmitted();
		wdt_disable();
		bl_main();
		break;
	case 90:
		sendOk1(adu->pdu.func, VERSION);
		break;
	default:
		if (handleStdCmd(&adu->pdu, len - 3))
			break;

		sendError(adu->pdu.func, ERR_UNKNOWN_CMD);
		break;
	}
}

