#include <avr/pgmspace.h>
#include "regs.h"
#include "ee.h"
#include "board.h"
#include "pwm.h"
#include "inputs.h"
#include "1w.h"
#include "adc.h"

static char adcs[] PROGMEM = { ADC0, ADC1, ADC2, ADC3, ADC0, ADC1, ADC2, ADC3 };

static EEMEM int16_t eepromRegisters[REG_EEPROM_CNT];
static int16_t ramRegisters[REG_RAM_CNT];

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
//	} else if (reg == REG_VMONOFF) {
//		*val = getvmonoff();
//	} else if (reg == REG_VMSTATE) {
//		*val = vmGetStatus();
	} else if (reg >= REG_ADC0 && reg < REG_ADC0 + REG_ADC_CNT) {
		*val = read_adc(pgm_read_byte(&(adcs[reg - REG_ADC0])), AVCC_VREF_TYPE);
	} else if (reg >= REG_IN0 && reg < REG_IN0 + REG_IN_CNT) {
		*val = getInput(reg - REG_IN0);
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
//	} else if (reg == REG_VMONOFF) {
//		setvmonoff(val);
//		startVM(val);
	} else if (reg >= REG_EEPROM0 && reg < REG_EEPROM0 + REG_EEPROM_CNT) {
		eeprom_update_word((uint16_t*) eepromRegisters + (reg - REG_EEPROM0),
				val);
	} else if (reg >= REG_INPULLUP0 && reg < REG_INPULLUP0 + REG_IN_CNT) {
		setPullup(reg - REG_INPULLUP0, val);
	} else if (reg >= REG_RAM0 && reg < REG_RAM0 + REG_RAM_CNT) {
		ramRegisters[reg] = val;
	} else {
		return 0;
	}
	return 1;
}

