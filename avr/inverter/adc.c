#include <avr/io.h>
#include <util/delay.h>
#include <stdlib.h>

#include "adc.h"

volatile uint8_t adc_data[LAST_ADC_INPUT - FIRST_ADC_INPUT + 1];

void initADC(void) {
	ADMUX = FIRST_ADC_INPUT | (ADC_VREF_TYPE & 0xff);
	ADCSRA |= (1 << ADPS2) | (1 << ADPS0) /*| (1 << ADIE)*/ | (1 << ADEN)
			| (1 << ADSC);
}


void adcStep() {
	static unsigned char input_index = 0;
	// Read the 8 most significant bits
	// of the AD conversion result

	if (ADCSRA & (1 << ADSC))
		return;

	adc_data[input_index] = ADCH;
	// Select next ADC input
	input_index++;
	if (input_index > (LAST_ADC_INPUT - FIRST_ADC_INPUT))
		input_index = 0;
	ADMUX = (FIRST_ADC_INPUT + input_index) | ADC_VREF_TYPE;

	// Delay needed for the stabilization of the ADC input voltage
	_delay_us(10);
	// Start the AD conversion
	ADCSRA |= (1 << ADSC);
}

