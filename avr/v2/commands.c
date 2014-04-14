#include <avr/pgmspace.h>
#include "regs.h"
#include "commands.h"
#include "packet.h"
#include "bl.h"
#include "rules.h"

static uint8_t regs[] PROGMEM
= {  REG_RELAYS, REG_INPUTS, REG_TEMP, REG_TEMP2, /*REG_VMONOFF, REG_VMSTATE,*/
REG_ADC0, REG_ADC1, REG_ADC2, REG_ADC3, REG_RAM0, REG_RAM1, REG_RAM2, REG_RAM3,
		REG_PWM0, REG_PWM1, REG_PWM2, REG_PWM3, REG_EEPROM0, REG_EEPROM1,
		REG_EEPROM2, REG_EEPROM3, };

typedef struct {
	uint8_t cmd;
	uint8_t _;
	uint8_t reg;
	uint8_t __;
	uint8_t q;
	uint8_t ___;
	int16_t data[];
} SetRegCmd;

typedef struct {
	uint8_t cmd;
	uint8_t _;
	uint8_t reg;
	uint8_t __;
	uint8_t n;
} GetRegCmd;

typedef struct {
	uint8_t cmd;
	uint8_t __;
	uint8_t n;
	uint8_t ___;
	int16_t data[];
} SetRuleCmd;

uint8_t handleStdCmd(PDU* pdu, uint8_t cmdlen) {
	uint8_t command = pdu->func;
	switch (command) {
	case CMD_MODBUS_SETREGS: {
		SetRegCmd* cmd1 = (SetRegCmd*) pdu;
		uint8_t reg = cmd1->reg;
		uint8_t n = cmd1->q;
		int16_t* data = cmd1->data;
		char res = 1;
		while (n--) {
			res &= setReg(reg++, BSWAP_16(*data));
			data++;
		}
		uint16_t S = sendPacketStart();
		if (res) {
			S = sendPacketBodyPart((uint8_t*) pdu, 5, S);
		} else {
			S = sendByte(command | 0x80, S);
			S = sendByte(2, S);
		}
		sendPacketEnd(S);
		return 1;
	}
	case CMD_MODBUS_GETREGS: {
		GetRegCmd* cmd1 = (GetRegCmd*) pdu;
		uint8_t reg = cmd1->reg;
		uint8_t n = cmd1->n;
		uint16_t S = sendPacketStart();
		S = sendByte(command, S);
		S = sendByte(n * 2, S);
		while (n--) {
			int val;
			getReg(reg++, &val);
			S = sendWord(val, S);
		}
		sendPacketEnd(S);
		return 1;
	}
	case CMD_GETALLREGS: {
		if (cmdlen != 1) {
			sendError(command, ERR_WRONG_CMD_FORMAT);
			return 1;
		}

		uint16_t S = sendPacketStart();
		S = sendByte(command, S);

		S = sendByte(0, S); // UI

		for (uint8_t i = 0; i < sizeof(regs) / sizeof(regs[0]); i++) {
			uint8_t reg = pgm_read_byte(regs + i);
			int16_t val = 0;
			getReg(reg, &val);
			S = sendByte(reg, S);
			S = sendWord(val, S);
		}

		sendPacketEnd(S);
		return 1;
	}
	case CMD_GETRULES: {
		uint16_t S = sendPacketStart();
		S = sendByte(command, S);

		S = sendByte(NRULES, S);
		int i;
		for (i = 0; i < NRULES; i++) {
			Rule rule;
			getRule(&rule, i);
			S = sendPacketBodyPart(&rule, sizeof(rule), S);
		}

		sendPacketEnd(S);
		return 1;
	}
	case CMD_SETRULE: {
		SetRuleCmd* cmd1 = (SetRuleCmd*) pdu;
		if (setRule(cmd1->data, cmd1->n))
			sendOk(command);
		else
			sendError(command, ERR_INVALID_PORT_NUM);
		return 1;
	}
	default: {
		return 0;
	}
	}
}
