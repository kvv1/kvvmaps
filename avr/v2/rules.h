#ifndef RULES_H_
#define RULES_H_

#include <stdint.h>

#define CONDITION_EQ 0
#define CONDITION_NE 1
#define CONDITION_LT 2
#define CONDITION_GT 3
#define CONDITION_LE 4
#define CONDITION_GE 5

#define NRULES 8


typedef struct {
	uint8_t enabled;
	uint8_t srcReg;
	uint8_t condition;
	int16_t srcValue;
	uint8_t dstReg;
	int16_t dstValue;
} Rule;

#endif /* RULES_H_ */
