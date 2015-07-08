/*
 * adc.h
 *
 *  Created on: 18.11.2014
 *      Author: kvv
 */

#ifndef ADC_H_
#define ADC_H_

#define ADC_VREF_TYPE 0x60

#define FIRST_ADC_INPUT 1
#define LAST_ADC_INPUT 4
extern volatile uint8_t adc_data[LAST_ADC_INPUT - FIRST_ADC_INPUT + 1];

void initADC(void);
void adcStep();

#endif /* ADC_H_ */
