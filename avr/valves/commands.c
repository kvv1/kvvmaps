#include "common.h"
#include "packet.h"
#include "commands.h"

#include <avr/eeprom.h>

#include "ee.h"

ee_8(TempOn);
ee_16(PrefTemp);
ee_16(PrefTemp2);

#define PORT_NUM 4
static PORTPIN ports[4] = { OUT0, OUT1, OUT2, OUT3 };
#define ADC_NUM 4
static char adcs[4] = { ADC0, ADC1, ADC2, ADC3 };

void sendOk() {
	char n = ERR_OK;
	sendPacket(&n, 1);
}

/*
 void sendOk1(char n) {
 char buf[2];
 buf[0] = ERR_OK;
 buf[1] = n;
 sendPacket(buf, 2);
 }
 */

void sendOkWord(int n) {
	char buf[3];
	buf[0] = ERR_OK;
	buf[1] = n >> 8;
	buf[2] = n;
	sendPacket(buf, 3);
}

void sendError(char err) {
	sendPacket(&err, 1);
}

void initCommands() {
	int tempOn = getTempOn();
	if (tempOn != 0 && tempOn != 1) {
		setTempOn(0);
		setPrefTemp(300);
		setPrefTemp2(400);
	}
}

static int getRelays() {
	char resp = 0;
	char b;
	b = getPin(OUT3) ? 1 : 0;
	resp |= b;
	resp <<= 1;
	b = getPin(OUT2) ? 1 : 0;
	resp |= b;
	resp <<= 1;
	b = getPin(OUT1) ? 1 : 0;
	resp |= b;
	resp <<= 1;
	b = getPin(OUT0) ? 1 : 0;
	resp |= b;
	return resp;
}

static char getReg(int reg, int* val) {
	if (reg < PORT_NUM) {
		*val = getPin(ports[reg]) ? 1 : 0;
		return 1;
	} else if (reg == REG_RELAYS) {
		*val = getRelays();
		return 1;
	} else if (reg == REG_TEMP_PREF) {
		*val = getPrefTemp();
		return 1;
	} else if (reg == REG_TEMP_PREF_2) {
		*val = getPrefTemp2();
		return 1;
	} else if (reg == REG_TEMP_PREF_ON) {
		*val = getTempOn();
		return 1;
	} else if (reg == REG_TEMP) {
		*val = getTemperature10();
		return 1;
	} else if (reg >= REG_ADC0 && reg < REG_ADC0 + ADC_NUM) {
		*val = read_adc(adcs[reg - REG_ADC0], AVCC_VREF_TYPE);
		return 1;
	} else {
		return 0;
	}
}

static char appendReg(char**p, char reg) {
	int val;
	char res = getReg(reg, &val);
	*((*p)++) = reg;
	*((*p)++) = val >> 8;
	*((*p)++) = val;
	return res;
}

void handleCmd(char* cmd, char cmdlen) {
//	print2("handleCmd %d %d ", *cmd, cmdlen);
	if (cmd[0] == CMD_SETREG && cmdlen == 4) {
		int reg = cmd[1];
		int val = ((int) cmd[2] << 8) + cmd[3];
		if (reg < PORT_NUM) {
			setPort(ports[reg], val);
			sendOk();
		} else if (reg == REG_RELAYS) {
			setPort(OUT0, val & 1);
			setPort(OUT1, val & 2);
			setPort(OUT2, val & 4);
			setPort(OUT3, val & 8);
			sendOk();
		} else if (reg == REG_TEMP_PREF) {
			setPrefTemp(val);
			sendOk();
		} else if (reg == REG_TEMP_PREF_2) {
			setPrefTemp2(val);
			sendOk();
		} else if (reg == REG_TEMP_PREF_ON) {
			setTempOn(val);
			startStopTemperatureControl();
			sendOk();
		} else {
			sendError(ERR_INVALID_PORT_NUM);
		}
	} else if (cmd[0] == CMD_GETREG && cmdlen == 2) {
		int val;
		char reg = cmd[1];
		if (getReg(reg, &val))
			sendOkWord(val);
		else
			sendError(ERR_INVALID_PORT_NUM);
	} else if (cmd[0] == CMD_GETREGS && cmdlen == 1) {
		static char buf[64];
		char* p = buf;
		char i;
		*(p++) = ERR_OK;

		for (i = 0; i < PORT_NUM; i++)
			appendReg(&p, i);

		appendReg(&p, REG_RELAYS);
		appendReg(&p, REG_TEMP_PREF);
		appendReg(&p, REG_TEMP_PREF_2);
		appendReg(&p, REG_TEMP_PREF_ON);
		appendReg(&p, REG_TEMP);

		for (i = 0; i < ADC_NUM; i++)
			appendReg(&p, REG_ADC0 + i);

		sendPacket(buf, p - buf);
	} else {
		sendError(ERR_UNKNOWN_CMD);
	}
}

