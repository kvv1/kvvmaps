#include "settings.h"
#include "packet.h"
#include "commands.h"
#include "valves.h"

#include "vm.h"
#include "vmstatus.h"
#include "crc16.h"

#include <avr/pgmspace.h>

#include "ee.h"

ee_16(CodeLen);
ee_16(CodeCRC);

static PORTPIN ports[] PROGMEM = { OUT0, OUT1, OUT2, OUT3, OUT0, OUT1, OUT2,
		OUT3 };
static PORTPIN pins[] PROGMEM = { IN0, IN1, IN0, IN1, IN0, IN1, IN0, IN1 };
static char adcs[] PROGMEM = { ADC0, ADC1, ADC2, ADC3, ADC0, ADC1, ADC2, ADC3 };

#define PORT(n) ((PORTPIN)pgm_read_word(ports + (n)))
#define PIN(n) ((PORTPIN)pgm_read_word(pins + (n)))

static EEMEM int16_t eepromRegisters[REG_EEPROM_CNT];
static int16_t ramRegisters[REG_RAM_CNT];

static EEMEM uint16_t pwm[REG_RELAY_CNT];
static uint8_t outState;
static uint16_t outCnt[REG_RELAY_CNT];

static void setPWM(uint8_t port, uint16_t value) {
	eeprom_update_word(pwm + port, value);
}

static uint16_t getPWM(uint8_t port) {
	return eeprom_read_word(pwm + port);
}

static void setOutput(uint8_t port, uint8_t state) {
	uint8_t mask = 1 << port;
	if (state) {
		outState |= mask;
		outCnt[port] = 0;
		setPort(PORT(port), getPWM(port) & 0xFF);
	} else {
		outState &= ~mask;
		setPort(PORT(port), 0);
	}
}

static uint16_t getOutput(uint8_t port) {
	return (outState >> port) & 1;
}

void handlePWM(int ms) {
	static uint16_t pwmCnt;

	pwmCnt += ms;
	if (pwmCnt >= 1000) {
		pwmCnt -= 1000;

		for (uint8_t i = 0; i < REG_RELAY_CNT; i++) {
			int8_t s;
			outCnt[i]++;
			if (outCnt[i] >= (getPWM(i) >> 8))
				outCnt[i] = 0;
			s = outCnt[i] < (getPWM(i) & 0xFF);
			setPort(PORT(i), s && getOutput(i));
		}
	}
}

int checkCode() {
	uint16_t S = CRC16_INIT;
	uint16_t len = getCodeLen();
	for (int i = 0; i < len; i++)
		S = crc16_step(vmReadByte(i), S);
	return S == getCodeCRC();
}

static void sendOk(uint8_t cmd) {
	sendPacket(&cmd, 1);
}

static void sendError(uint8_t cmd, uint8_t err) {
	uint16_t S = sendPacketStart();
	S = sendByte(cmd | 0x80, S);
	S = sendByte(err, S); // unsupported function
	sendPacketEnd(S);
}

static int getRelays() {
	return outState;
}

static void setRelays(uint8_t val) {
	for (uint8_t n = 0; n < REG_RELAY_CNT; n++) {
		setOutput(n, val & 1);
		val >>= 1;
	}
}

void onVMStatusChanged() {
	setRelays(0);
}

void initCommands() {
	uint8_t n = REG_RELAY_CNT;
	while (n--)
		confPin(PORT(n), PIN_OUT, 0);
	setRelays(0);

	if (getCodeLen() == 0xFFFF) {
		setCodeLen(0);
		setCodeCRC(CRC16_INIT);
	}
}

static int getInputs() {
	char resp = 0;
	uint8_t n = REG_IN_CNT;
	while (n--) {
		resp <<= 1;
		resp |= getPin(PIN(n));
	}
	return resp;
}
/*
char getReg(int reg, int* val) {
	switch(reg) {
	case REG_RELAYS:
		*val = getRelays();
		break;
	case REG_INPUTS:
		*val = getInputs();
		break;
	case REG_TEMP:
		*val = temperature[0];
		break;
	case REG_TEMP2:
		*val = temperature[1];
		break;
	case REG_VMONOFF:
		*val = getvmonoff();
		break;
	case REG_VMSTATE:
		*val = vmGetStatus();
		break;
	default:
		if (reg >= REG_RELAY0 && reg < REG_RELAY0 + REG_RELAY_CNT) {
			*val = getOutput(reg - REG_RELAY0) ? 1 : 0;
		} else if (reg >= REG_PWM0 && reg < REG_PWM0 + REG_RELAY_CNT) {
			*val = getPWM(reg - REG_PWM0);
		} else if (reg >= REG_ADC0 && reg < REG_ADC0 + REG_ADC_CNT) {
			*val = read_adc(pgm_read_byte(&(adcs[reg - REG_ADC0])), AVCC_VREF_TYPE);
		} else if (reg >= REG_IN0 && reg < REG_IN0 + REG_IN_CNT) {
			*val = getPin(PIN(reg - REG_IN0)) ? 1 : 0;
		} else if (reg >= REG_EEPROM0 && reg < REG_EEPROM0 + REG_EEPROM_CNT) {
			*val = eeprom_read_word(
					(uint16_t*) eepromRegisters + (reg - REG_EEPROM0));
		} else if (reg >= REG_RAM0 && reg < REG_RAM0 + REG_RAM_CNT) {
			*val = ramRegisters[reg];
		} else {
			return 0;
		}
		break;
	}
	return 1;
}
*/
char getReg(uint8_t reg, int* val) {
	if (reg >= REG_RELAY0 && reg < REG_RELAY0 + REG_RELAY_CNT) {
		*val = getOutput(reg - REG_RELAY0) ? 1 : 0;
	} else if (reg >= REG_PWM0 && reg < REG_PWM0 + REG_RELAY_CNT) {
		*val = getPWM(reg - REG_PWM0);
	} else if (reg == REG_RELAYS) {
		*val = getRelays();
	} else if (reg == REG_INPUTS) {
		*val = getInputs();
	} else if (reg == REG_TEMP) {
		*val = w1_temp(0);
	} else if (reg == REG_TEMP2) {
		*val = w1_temp(1);
	} else if (reg == REG_VMONOFF) {
		*val = getvmonoff();
	} else if (reg == REG_VMSTATE) {
		*val = vmGetStatus();
	} else if (reg >= REG_ADC0 && reg < REG_ADC0 + REG_ADC_CNT) {
		*val = read_adc(pgm_read_byte(&(adcs[reg - REG_ADC0])), AVCC_VREF_TYPE);
	} else if (reg >= REG_IN0 && reg < REG_IN0 + REG_IN_CNT) {
		*val = getPin(PIN(reg - REG_IN0)) ? 1 : 0;
	} else if (reg >= REG_EEPROM0 && reg < REG_EEPROM0 + REG_EEPROM_CNT) {
		*val = eeprom_read_word(
				(uint16_t*) eepromRegisters + (reg - REG_EEPROM0));
	} else if (reg >= REG_RAM0 && reg < REG_RAM0 + REG_RAM_CNT) {
		*val = ramRegisters[reg];
	} else {
		return 0;
	}
	return 1;
}

char setReg(uint8_t reg, int val) {
	if (reg >= REG_RELAY0 && reg < REG_RELAY0 + REG_RELAY_CNT) {
		setOutput(reg - REG_RELAY0, val);
	} else if (reg >= REG_PWM0 && reg < REG_PWM0 + REG_RELAY_CNT) {
		setPWM(reg - REG_PWM0, val);
	} else if (reg == REG_RELAYS) {
		setRelays(val);
	} else if (reg == REG_VMONOFF) {
		setvmonoff(val);
		startVM(val);
	} else if (reg >= REG_EEPROM0 && reg < REG_EEPROM0 + REG_EEPROM_CNT) {
		eeprom_update_word((uint16_t*) eepromRegisters + (reg - REG_EEPROM0),
				val);
	} else if (reg >= REG_INPULLUP0 && reg < REG_INPULLUP0 + REG_IN_CNT) {
		setPort(PIN(reg - REG_INPULLUP0), val);
	} else if (reg >= REG_RAM0 && reg < REG_RAM0 + REG_RAM_CNT) {
		ramRegisters[reg] = val;
	} else {
		return 0;
	}
	return 1;
}

static uint8_t regs[] PROGMEM
= { REG_RELAYS, REG_INPUTS, REG_TEMP, REG_TEMP2, REG_VMONOFF, REG_VMSTATE,
		REG_ADC0, REG_ADC1, REG_ADC2, REG_ADC3, REG_RAM0, REG_RAM1, REG_RAM2,
		REG_RAM3, REG_PWM0, REG_PWM1, REG_PWM2, REG_PWM3, REG_EEPROM0,
		REG_EEPROM1, REG_EEPROM2, REG_EEPROM3, };

void handleCmd(uint8_t* cmd, uint8_t cmdlen) {
//	print2("handleCmd %d %d ", *cmd, cmdlen);
	uint8_t command = cmd[0];
	switch (command) {
	case CMD_MODBUS_SETREGS: {
		uint8_t reg = cmd[2]; // fetch(cmd + 1); //(cmd[1] << 8) + cmd[2];
		uint8_t n = cmd[4];
		uint8_t* data = cmd + 6;
		char res = 1;
		while (n--) {
			res &= setReg(reg++, fetch(data));
			data += 2;
		}
		uint16_t S = sendPacketStart();
		if (res) {
			S = sendPacketBodyPart(cmd, 5, S);
		} else {
			S = sendByte(command | 0x80, S);
			S = sendByte(2, S);
		}
		sendPacketEnd(S);
		break;
	}
	case CMD_MODBUS_GETREGS: {
		uint8_t reg = cmd[2]; // fetch(cmd + 1);
		uint8_t n = cmd[4];
		uint16_t S = sendPacketStart();
		S = sendByte(command, S);
		S = sendByte(n * 2, S);
		while (n--) {
			int val;
			getReg(reg++, &val);
			S = sendWord(val, S);
		}
		sendPacketEnd(S);
		break;
	}
	case CMD_GETALLREGS: {
		if (cmdlen != 1) {
			sendError(command, ERR_WRONG_CMD_FORMAT);
			return;
		}

		uint16_t S = sendPacketStart();
		S = sendByte(command, S);

		if (getCodeLen() == 0)
			S = sendByte(0, S);
		else {
			int uiEnd = getUIEnd();
			for (int i = getUIStart(); i < uiEnd; i++)
				S = sendByte(vmReadByte(i), S);
		}

		for (uint8_t i = 0; i < sizeof(regs) / sizeof(regs[0]); i++) {
			uint8_t reg = pgm_read_byte(regs + i);
			int16_t val;
			getReg(reg, &val);
			S = sendByte(reg, S);
			S = sendWord(val, S);
		}

		sendPacketEnd(S);
		break;
	}
	case CMD_UPLOAD: {
		uint16_t addr;
		if (cmdlen < 3) {
			sendError(command, ERR_WRONG_CMD_FORMAT);
			return;
		}

		cmdlen -= 3;

		addr = fetch(cmd + 1);

		if (addr + cmdlen > VMCODE_SIZE) {
			sendError(command, ERR_WRONG_CMD_FORMAT);
			return;
		}

		setvmonoff(0);
		startVM(0);
		EEPROM_writeBlock((int) code + addr, cmdlen, cmd + 3);
		sendOk(command);
		break;
	}
	case CMD_UPLOAD_END: {
		uint16_t codeLen = fetch(cmd + 1);
		uint16_t codeCRC = fetch(cmd + 3);
		if (codeLen == 0) {
			setCodeLen(codeLen);
			sendOk(command);
			return;
		}

		if (codeLen > VMCODE_SIZE) {
			setCodeLen(0);
			sendError(command, ERR_WRONG_CMD_FORMAT);
			return;
		}

		setCodeLen(codeLen);
		setCodeCRC(codeCRC);

		if (!checkCode()) {
			setCodeLen(0);
			sendError(command, ERR_WRONG_CMD_FORMAT);
			return;
		}

		vmInit();
		sendOk(command);
		break;
	}
	case CMD_VMINIT: {
		if (!checkCode()) {
			setCodeLen(0);
			sendError(command, ERR_WRONG_CMD_FORMAT);
			return;
		}
		vmInit();
		sendOk(command);
		break;
	}
	default: {
		sendError(command, ERR_UNKNOWN_CMD);
		break;
	}
	}
}

