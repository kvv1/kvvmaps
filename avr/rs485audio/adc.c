#include <avr/io.h>
#include <stdint.h>
#include <util/delay.h>

#include "adc.h"

#define AVCC_VREF_TYPE 0x40 // AVCC
void adcInit() {
	// ADC initialization
	// ADC Clock frequency: 1000,000 kHz
	// ADC Voltage Reference: AVCC pin
	// ADC Auto Trigger Source: None
	int prescaler = 3;
	ADCSRA = (1 << ADEN) | prescaler;

	unsigned char adc_input = 1;
	unsigned char vref_type = AVCC_VREF_TYPE;
	ADMUX = adc_input | vref_type | (1 << ADLAR);
}

uint8_t readAdc() {
	static uint8_t minVal = 255;
	static uint8_t maxVal = 0;

	static uint16_t n;

	uint8_t cur = ADCH;
	// Start the AD conversion
	ADCSRA |= (1 << ADSC);

	if (cur < minVal)
		minVal = cur;

	if (cur > maxVal)
		maxVal = cur;

	if (!n && (minVal < maxVal)) {
		minVal++;
		maxVal--;
	}

	if (n == 0)
		n = 1000;
	n--;

	return cur > (minVal >> 1) + (maxVal >> 1);
}

