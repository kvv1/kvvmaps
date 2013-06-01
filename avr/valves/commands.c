#include "common.h"
#include "packet.h"
#include "commands.h"
#include "valves.h"

#include "vm.h"
#include "vmstatus.h"

#include <avr/pgmspace.h>

#include "ee.h"

ee_8(TempOn);
ee_16(PrefTemp);
ee_16(PrefTemp2);

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

void initCommands() {
	int n = REG_RELAY_CNT;
	while (n--)
		confPin(PORT(n), PIN_OUT, 0);

	if (getCodeLen() == 0xFFFF) {
		setCodeLen(0);
		setCodeCRC(CRC16_INIT);
	}

	if (getTempOn() == 255)
		setTempOn(0);

	if (getPrefTemp() == 0xFFFF)
		setPrefTemp(300);

	if (getPrefTemp2() == 0xFFFF)
		setPrefTemp2(400);
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
	char resp = 0;
	int n = REG_RELAY_CNT;
	while (n--) {
		resp <<= 1;
		resp |= getPin(PORT(n));
	}
	return resp;
}

static void setRelays(int val) {
	int n;
	for (n = 0; n < REG_RELAY_CNT; n++) {
		setPort(PORT(n), val & 1);
		val >>= 1;
	}
}

static int getIns() {
	char resp = 0;
	int n = REG_IN_CNT;
	while (n--) {
		resp <<= 1;
		resp |= getPin(PIN(n));
	}
	return resp;
}

char getReg(int reg, int* val) {
	if (reg >= REG_RELAY0 && reg < REG_RELAY0 + REG_RELAY_CNT) {
		*val = getPin(PORT(reg - REG_RELAY0)) ? 1 : 0;
	} else if (reg == REG_RELAYS) {
		*val = getRelays();
	} else if (reg == REG_INPUTS) {
		*val = getIns();
	} else if (reg == REG_TEMP_PREF) {
		*val = getPrefTemp();
	} else if (reg == REG_TEMP_PREF_2) {
		*val = getPrefTemp2();
	} else if (reg == REG_TEMP_PREF_ON) {
		*val = getTempOn();
	} else if (reg == REG_TEMP) {
		*val = getTemperature10();
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

char setReg(int reg, int val) {
	if (reg >= REG_RELAY0 && reg < REG_RELAY0 + REG_RELAY_CNT) {
		setPort(PORT(reg - REG_RELAY0), val);
	} else if (reg == REG_RELAYS) {
		setRelays(val);
	} else if (reg == REG_TEMP_PREF) {
		setPrefTemp(val);
	} else if (reg == REG_TEMP_PREF_2) {
		setPrefTemp2(val);
	} else if (reg == REG_TEMP_PREF_ON) {
		setTempOn(val);
		startStopTemperatureControl();
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

static uint8_t regs[] PROGMEM = { REG_RELAYS, REG_INPUTS, REG_TEMP_PREF,
		REG_TEMP_PREF_2, REG_TEMP_PREF_ON, REG_TEMP, REG_VMONOFF, REG_VMSTATE,
		REG_ADC0, REG_ADC1, REG_ADC2, REG_ADC3, REG_RAM0, REG_RAM1, REG_RAM2,
		REG_RAM3, REG_EEPROM0, REG_EEPROM1, REG_EEPROM2, REG_EEPROM3, };

void handleCmd(uint8_t* cmd, uint8_t cmdlen) {
//	print2("handleCmd %d %d ", *cmd, cmdlen);
	uint8_t command = cmd[0];
	switch (command) {
	case CMD_MODBUS_SETREGS: {
		int reg = (cmd[1] << 8) + cmd[2];
		int n = cmd[4];
		uint8_t* data = cmd + 6;
		char res = 1;
		while (n--) {
			res &= setReg(reg++, (data[0] << 8) | data[1]);
			data += 2;
		}
		uint16_t S = sendPacketStart();
		if (res) {
			S = sendPacketBodyPart(cmd, 5, S);
		} else {
			static uint8_t resp[] = { CMD_MODBUS_SETREGS | 0x80, 2 };
			S = sendPacketBodyPart(resp, 2, S);
		}
		sendPacketEnd(S);
		break;
	}
	case CMD_MODBUS_GETREGS: {
		int reg = (cmd[1] << 8) + cmd[2];
		int n = cmd[4];
		uint16_t S = sendPacketStart();
		S = sendByte(command, S);
		S = sendByte(n * 2, S);
		while (n--) {
			int val;
			getReg(reg++, &val);
			S = sendByte(val >> 8, S);
			S = sendByte(val, S);
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

		for (int i = 0; i < sizeof(regs) / sizeof(regs[0]); i++) {
			uint8_t reg = pgm_read_byte(regs + i);
			int16_t val;
			getReg(reg, &val);
			uint8_t buf[3];
			buf[0] = reg;
			buf[1] = val >> 8;
			buf[2] = val;
			S = sendPacketBodyPart(buf, 3, S);
		}

		sendPacketEnd(S);
		break;
	}
	case CMD_UPLOAD: {
		uint16_t addr;
		int len = cmdlen - 3;
		if (len < 0) {
			sendError(command, ERR_WRONG_CMD_FORMAT);
			return;
		}
		addr = (cmd[1] << 8) + cmd[2];

		if (addr + len > VMCODE_SIZE) {
			sendError(command, ERR_WRONG_CMD_FORMAT);
			return;
		}

		setvmonoff(0);
		startVM(0);
		EEPROM_writeBlock((int) code + addr, len, cmd + 3);
		sendOk(command);
		break;
	}
	case CMD_UPLOAD_END: {
		uint16_t codeLen = (cmd[1] << 8) + cmd[2];
		uint16_t codeCRC = (cmd[3] << 8) + cmd[4];
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

		sendOk(command);
		break;
	}
	default: {
		sendError(command, ERR_UNKNOWN_CMD);
		break;
	}
	}
}

