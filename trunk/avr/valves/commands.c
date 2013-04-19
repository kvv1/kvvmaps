#include "common.h"
#include "packet.h"
#include "commands.h"
#include "valves.h"

#include "vm.h"
#include "vmstatus.h"

#include <avr/eeprom.h>
#include <avr/pgmspace.h>

#include "ee.h"

ee_8(TempOn);
ee_16(PrefTemp);
ee_16(PrefTemp2);

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

	if (getTempOn() == 255)
		setTempOn(0);

	if (getPrefTemp() == 0xFFFF)
		setPrefTemp(300);

	if (getPrefTemp2() == 0xFFFF)
		setPrefTemp2(400);
}

void sendOk() {
	char n = ERR_OK;
	sendPacket(&n, 1);
}

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

//static char appendReg(char**p, char reg) {
//	int val;
//	char res = getReg(reg, &val);
//	*((*p)++) = reg;
//	*((*p)++) = val >> 8;
//	*((*p)++) = val;
//	return res;
//}

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

void handleCmd(char* cmd, uint8_t cmdlen) {
//	print2("handleCmd %d %d ", *cmd, cmdlen);
	switch (cmd[0]) {
	case CMD_SETREG: {
		int reg = cmd[1];
		int val = ((int) cmd[2] << 8) + cmd[3];
		if (cmdlen != 4) {
			sendError(ERR_WRONG_CMD_FORMAT);
		} else if (setReg(reg, val)) {
			sendOk();
		} else {
			sendError(ERR_INVALID_PORT_NUM);
		}
		break;
	}
	case CMD_GETREG: {
		int val;
		char reg = cmd[1];
		if (cmdlen != 2) {
			sendError(ERR_WRONG_CMD_FORMAT);
		} else if (getReg(reg, &val))
			sendOkWord(val);
		else
			sendError(ERR_INVALID_PORT_NUM);
		break;
	}
	case CMD_GETREGS: {
		if (cmdlen != 1) {
			sendError(ERR_WRONG_CMD_FORMAT);
			return;
		}

		uint8_t S = 0;
		int len = 1 + 3 * sizeof(regs) / sizeof(regs[0]);

		if (len > 250) {
			sendError(ERR_TOO_LONG_PACKET);
			return;
		}

		sendPacketStart(len, &S);
		uint8_t errcode = ERR_OK;
		sendPacketBodyPart(&errcode, 1, &S);

		for (int i = 0; i < sizeof(regs) / sizeof(regs[0]); i++) {
			uint8_t reg = pgm_read_byte(regs + i);
			int16_t val;
			getReg(reg, &val);
			uint8_t buf[3];
			buf[0] = reg;
			buf[1] = val >> 8;
			buf[2] = val;
			sendPacketBodyPart(buf, 3, &S);
		}

		sendPacketEnd(&S);
		break;
	}
	case CMD_UPLOAD: {
		uint16_t addr;
		int len = cmdlen - 3;
		if (len < 0) {
			sendError(ERR_WRONG_CMD_FORMAT);
			return;
		}
		addr = (cmd[1] << 8) + cmd[2];

		if (addr + len > VMCODE_SIZE) {
			sendError(ERR_CODESIZE);
			return;
		}

		startVM(0);

		eeprom_update_block(cmd + 3, code + addr, len);
		getdummy();
		sendOk();
		break;
	}
	case CMD_DOWNLOAD: {
		if (cmdlen != 1) {
			sendError(ERR_WRONG_CMD_FORMAT);
			return;
		}

		uint8_t S = 0;
		int len = 1 + vmCheckCode();
		sendPacketStart(len, &S);
		uint8_t errcode = ERR_OK;
		sendPacketBodyPart(&errcode, 1, &S);

		len--;

		for (int i = 0; i < len; i++) {
			uint8_t b = vmReadByte(i);
			sendPacketBodyPart(&b, 1, &S);
		}

		sendPacketEnd(&S);
		break;
	}
	default:
		sendError(ERR_UNKNOWN_CMD);
		break;
	}
}

