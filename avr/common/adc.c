#include "common.h"
#include "util/atomic.h"
#include "util/delay.h"

// Read the AD conversion result
unsigned int read_adc(unsigned char adc_input, unsigned char vref_type) {
	int res;
	ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {

		ADMUX = adc_input | vref_type;
		// Delay needed for the stabilization of the ADC input voltage
		_delay_us(10);
		// Start the AD conversion
		ADCSRA |= (1 << ADSC);
		// Wait for the AD conversion to complete
		while (ADCSRA & (1 << ADSC))
			;

		res = ADCW;
	}
	return res;
}

#if(F_CPU == 1000000)
#define PRESCALER 1
#endif
#if(F_CPU == 2000000)
#define PRESCALER 1
#endif
#if(F_CPU == 4000000)
#define PRESCALER 2
#endif
#if(F_CPU == 8000000)
#define PRESCALER 3
#endif


void init_adc(char inputs, char vref_type) {
	// ADC initialization
	// ADC Clock frequency: 1000,000 kHz
	// ADC Voltage Reference: AVCC pin
	// ADC Auto Trigger Source: None
#if defined(__AVR_ATmega48__) || defined(__AVR_ATmega168__)
	DIDR0=inputs;
#endif
	ADMUX = vref_type;
	ADCSRA = (1 << ADEN) | PRESCALER;
}

