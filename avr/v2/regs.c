#include <avr/pgmspace.h>
#include <avr/interrupt.h>
#include "regs.h"
#include "hw.h"
#include "ee.h"
#include "board.h"
#include "pwm.h"
#include "inputs.h"
#include "1w.h"
#include "adc.h"
#include "stepper.h"
#include "rules1.h"

static char adcs[REG_ADC_CNT] PROGMEM = { ADC0, ADC1, ADC2, ADC3 };

static int16_t ramRegisters[REG_RAM_CNT];

static uint32_t timeBuffer;

char getReg(uint16_t reg, int* val) {
	if (reg >= REG_RELAY0 && reg < REG_RELAY0 + REG_RELAY_CNT) {
		*val = getOutput(reg - REG_RELAY0) ? 1 : 0;
	} else if (reg >= REG_PWM0 && reg < REG_PWM0 + REG_RELAY_CNT) {
		*val = getPWM(reg - REG_PWM0);
	} else if (reg == REG_TEMP) {
		*val = w1_temp(0);
		if (*val == TEMPERATURE_INVALID)
			return 0;
	} else if (reg == REG_ADC_CONF) {
		*val = EEPROM_readByte(&eeData.adcconf);
	} else if (reg == REG_RESET_BY_WD) {
		*val = EEPROM_readByte(&eeData.resetByWd);
	} else if (reg == REG_WD_ON_RECEIVE) {
		*val = EEPROM_readByte(&eeData.wdOnReceive);
	} else if (reg == REG_TIME_CORR_EN) {
		*val = EEPROM_readByte(&eeData.timeCorrEn);
	} else if (reg == REG_TIME_CORR) {
		*val = getCorr1();
	} else if (reg == REG_TIME_HI) {
		timeBuffer = getTime();
		*val = timeBuffer >> 16;
	} else if (reg == REG_TIME_LO) {
		*val = timeBuffer;
	} else if (reg >= REG_ADC0 && reg < REG_ADC0 + REG_ADC_CNT) {
		if (EEPROM_readByte(&eeData.adcconf) & (1 << (reg - REG_ADC0))) {
			*val = w1_temp(reg - REG_ADC0 + 1);
			if (*val == TEMPERATURE_INVALID)
				return 0;
		} else {
			*val = read_adc(pgm_read_byte(&(adcs[reg - REG_ADC0])),
					AVCC_VREF_TYPE);
		}
	} else if (reg >= REG_IN0 && reg < REG_IN0 + REG_IN_CNT) {
		*val = getInput(reg - REG_IN0);
	} else if (reg >= REG_EEPROM0 && reg < REG_EEPROM0 + REG_EEPROM_CNT) {
		*val = EEPROM_readWord(eeData.eepromRegisters + (reg - REG_EEPROM0));
	} else if (reg >= REG_RAM0 && reg < REG_RAM0 + REG_RAM_CNT) {
		*val = ramRegisters[reg - REG_RAM0];
	} else if (reg >= REG_STEPPER0_START && reg < REG_STEPPER0_END) {
		*val = stepperGetReg(0, reg - REG_STEPPER0_START);
	} else if (reg >= REG_STEPPER1_START && reg < REG_STEPPER1_END) {
		*val = stepperGetReg(1, reg - REG_STEPPER1_START);
	} else if (reg == REG_RULES_SIZE) {
		*val = getRules1Size();
	} else if (reg == REG_RULES_STATE) {
		*val = getRulesState();
	} else if (reg >= REG_RULES && reg < REG_RULES + REG_RULES_CNT) {
		*val = getRules1Word(reg - REG_RULES);
	} else {
		return 0;
	}
	return 1;
}

char setReg(uint16_t reg, int val) {
	if (reg >= REG_RELAY0 && reg < REG_RELAY0 + REG_RELAY_CNT) {
		setOutput(reg - REG_RELAY0, val);
	} else if (reg >= REG_PWM0 && reg < REG_PWM0 + REG_RELAY_CNT) {
		setPWM(reg - REG_PWM0, val);
	} else if (reg >= REG_EEPROM0 && reg < REG_EEPROM0 + REG_EEPROM_CNT) {
		EEPROM_writeWord(eeData.eepromRegisters + (reg - REG_EEPROM0), val);
	} else if (reg >= REG_INPULLUP0 && reg < REG_INPULLUP0 + REG_IN_CNT) {
		setPullup(reg - REG_INPULLUP0, val);
	} else if (reg >= REG_RAM0 && reg < REG_RAM0 + REG_RAM_CNT) {
		ramRegisters[reg - REG_RAM0] = val;
	} else if (reg == REG_ADC_CONF) {
		EEPROM_writeByte(&eeData.adcconf, val);
	} else if (reg == REG_RESET_BY_WD) {
		EEPROM_writeByte(&eeData.resetByWd, val);
	} else if (reg == REG_WD_ON_RECEIVE) {
		EEPROM_writeByte(&eeData.wdOnReceive, val);
	} else if (reg >= REG_STEPPER0_START && reg < REG_STEPPER0_END) {
		stepperSetReg(0, reg - REG_STEPPER0_START, val);
	} else if (reg >= REG_STEPPER1_START && reg < REG_STEPPER1_END) {
		stepperSetReg(1, reg - REG_STEPPER1_START, val);
	} else if (reg == REG_TIME_CORR_EN) {
		EEPROM_writeByte(&eeData.timeCorrEn, val);
	} else if (reg == REG_TIME_HI) {
		timeBuffer = val;
	} else if (reg == REG_TIME_LO) {
		timeBuffer = (timeBuffer << 16) | (uint32_t)(uint16_t) val;
		setTime(timeBuffer);
	} else {
		return 0;
	}
	return 1;
}

