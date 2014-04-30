#ifndef RULES_H_
#define RULES_H_

#include <stdint.h>

#define CONDITION_INVALID 0
#define CONDITION_EQ 1
#define CONDITION_NE 2
#define CONDITION_LT 3
#define CONDITION_GT 4
#define CONDITION_LE 5
#define CONDITION_GE 6

#define NRULES 8


typedef struct {
	uint8_t enabled;
	uint8_t srcReg;
	uint8_t condition;
	int16_t srcValue;
	uint8_t dstReg;
	int16_t dstValue;
} Rule;

uint8_t setRule(Rule* rule, uint8_t n);
void getRule(Rule* rule, uint8_t n);
void stepRules();

#endif /* RULES_H_ */
