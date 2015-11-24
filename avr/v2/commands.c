#include <avr/pgmspace.h>
#include "regs.h"
#include "commands.h"
#include "packet.h"
#include "bl.h"
#include "rules.h"
#include "rules1.h"

typedef struct {
	uint8_t cmd;
	uint8_t _regHi;
	uint8_t reg;
	uint8_t _qHi;
	uint8_t q;
	uint8_t _bytes;
	int16_t data[];
} SetRegCmd;

typedef struct {
	uint8_t cmd;
	uint8_t _regHi;
	uint8_t reg;
	uint8_t _nHi;
	uint8_t n;
} GetRegCmd;

typedef struct {
	uint8_t cmd;
	uint8_t n;
	Rule rule;
} SetRuleCmd;

uint8_t handleStdCmd(PDU* pdu, uint8_t cmdlen) {
	uint8_t command = pdu->func;
	switch (command) {
	case CMD_MODBUS_SETREGS: {
		SetRegCmd* cmd1 = (SetRegCmd*) pdu;
		uint16_t reg = GET_BIGENDIAN(&cmd1->_regHi);
		uint8_t n = cmd1->q;
		int16_t* data = cmd1->data;
		char res = 1;

		if (reg == REG_RULES) {
			for(int i = 0; i < n; i++) {
				setRules1Word(i, BSWAP_16(data[i]));
			}
//			setRules1(data, n);
		} else {
			while (n--) {
				res &= setReg(reg++, BSWAP_16(*data));
				data++;
			}
		}

		if (res) {
			uint16_t S = sendPacketStart();
			S = sendPacketBodyPart(pdu, 5, S);
			sendPacketEnd(S);
		} else {
			sendError(command, ERR_INVALID_PORT_NUM);
		}
		return 1;
	}
	case CMD_MODBUS_GETREGS: {
		GetRegCmd* cmd1 = (GetRegCmd*) pdu;
		uint16_t reg = GET_BIGENDIAN(&cmd1->_regHi);
		uint8_t n = cmd1->n;
		uint16_t S = sendPacketStart();
		S = sendByte(command, S);
		S = sendByte(n * 2, S);
		while (n--) {
			int val = 0;
			getReg(reg++, &val);
			S = sendWord(val, S);
		}
		sendPacketEnd(S);
		return 1;
	}
	case CMD_GETRULES: {
		uint16_t S = sendPacketStart();
		S = sendByte(command, S);
		int i;
		for (i = 0; i < NRULES; i++) {
			Rule rule;
			getRule(&rule, i);
			rule.srcValue = BSWAP_16(rule.srcValue);
			rule.dstValue = BSWAP_16(rule.dstValue);
			S = sendPacketBodyPart(&rule, sizeof(rule), S);
		}

		sendPacketEnd(S);
		return 1;
	}
	case CMD_SETRULE: {
		SetRuleCmd* cmd1 = (SetRuleCmd*) pdu;
		cmd1->rule.srcValue = BSWAP_16(cmd1->rule.srcValue);
		cmd1->rule.dstValue = BSWAP_16(cmd1->rule.dstValue);
		if (setRule(&cmd1->rule, cmd1->n))
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
