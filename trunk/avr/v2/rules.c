#include <avr/pgmspace.h>
#include "ee.h"
#include "regs.h"
#include "rules.h"

static EEMEM Rule rules[NRULES];

uint8_t setRule(Rule* rule, uint8_t i) {
	if (i >= NRULES)
		return 0;
	EEPROM_writeBlock((uint16_t) (rules + i), sizeof(*rule), (uint8_t*) rule);
	return 1;
}

void getRule(Rule* rule, uint8_t i) {
	EEPROM_readBlock((uint16_t) (rules + i), sizeof(*rule), (uint8_t*) rule);
}

void execRule(uint8_t n) {
	Rule rule;
	getRule(&rule, n);
	if (rule.enabled != 1)
		return;

	int16_t val;
	if (!getReg(rule.srcReg, &val))
		return;

	if ((rule.condition == CONDITION_EQ && val == rule.srcValue)
			|| (rule.condition == CONDITION_NE && val != rule.srcValue)
			|| (rule.condition == CONDITION_LT && val < rule.srcValue)
			|| (rule.condition == CONDITION_GT && val > rule.srcValue)
			|| (rule.condition == CONDITION_LE && val <= rule.srcValue)
			|| (rule.condition == CONDITION_GE && val >= rule.srcValue))
		setReg(rule.dstReg, rule.dstValue);

}
