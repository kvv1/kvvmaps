#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/delay.h>
#include <stdlib.h>
#include <avr/pgmspace.h>

#include "adc.h"

#define ADC1 adc_data[1]
#define ADC2 adc_data[0]

//#define ADC1 255
//#define ADC2 adc_data[0]

#define U0 311U // 3.11
#define ENBITS ((1 << PD6) | (1 << PD7))
#define EN ((PIND & ENBITS) == ENBITS)
#define configEn() (PORTD |= ENBITS)

//#define SIGN(b) do { if(b) PORTB |= (1 << PB0); else PORTB &= ~(1 << PB0); } while(0)
#define changeSign() (PORTB ^= (1 << PB0))
#define configSign() (DDRB |= (1 << PB0))

#define TOP 80

uint8_t sintable[] PROGMEM = { 1, 2, 2, 3, 4, 5, 6, 6, 7, 8, 9, 10, 10, 11, 12,
		13, 14, 14, 15, 16, 17, 18, 18, 19, 20, 21, 22, 22, 23, 24, 25, 26, 26,
		27, 28, 29, 30, 30, 31, 32, 33, 34, 34, 35, 36, 37, 38, 38, 39, 40, 41,
		41, 42, 43, 44, 45, 45, 46, 47, 48, 49, 49, 50, 51, 52, 52, 53, 54, 55,
		56, 56, 57, 58, 59, 60, 60, 61, 62, 63, 63, 64, 65, 66, 67, 67, 68, 69,
		70, 70, 71, 72, 73, 73, 74, 75, 76, 77, 77, 78, 79, 80, 80, 81, 82, 83,
		83, 84, 85, 86, 86, 87, 88, 89, 89, 90, 91, 92, 92, 93, 94, 95, 95, 96,
		97, 98, 98, 99, 100, 101, 101, 102, 103, 103, 104, 105, 106, 106, 107,
		108, 109, 109, 110, 111, 111, 112, 113, 114, 114, 115, 116, 116, 117,
		118, 119, 119, 120, 121, 121, 122, 123, 124, 124, 125, 126, 126, 127,
		128, 128, 129, 130, 130, 131, 132, 133, 133, 134, 135, 135, 136, 137,
		137, 138, 139, 139, 140, 141, 141, 142, 143, 143, 144, 145, 145, 146,
		147, 147, 148, 149, 149, 150, 151, 151, 152, 152, 153, 154, 154, 155,
		156, 156, 157, 158, 158, 159, 159, 160, 161, 161, 162, 163, 163, 164,
		164, 165, 166, 166, 167, 167, 168, 169, 169, 170, 170, 171, 172, 172,
		173, 173, 174, 175, 175, 176, 176, 177, 177, 178, 179, 179, 180, 180,
		181, 181, 182, 183, 183, 184, 184, 185, 185, 186, 186, 187, 188, 188,
		189, 189, 190, 190, 191, 191, 192, 192, 193, 193, 194, 194, 195, 195,
		196, 196, 197, 197, 198, 199, 199, 200, 200, 201, 201, 201, 202, 202,
		203, 203, 204, 204, 205, 205, 206, 206, 207, 207, 208, 208, 209, 209,
		210, 210, 210, 211, 211, 212, 212, 213, 213, 214, 214, 214, 215, 215,
		216, 216, 217, 217, 217, 218, 218, 219, 219, 219, 220, 220, 221, 221,
		222, 222, 222, 223, 223, 223, 224, 224, 225, 225, 225, 226, 226, 226,
		227, 227, 228, 228, 228, 229, 229, 229, 230, 230, 230, 231, 231, 231,
		232, 232, 232, 233, 233, 233, 234, 234, 234, 235, 235, 235, 236, 236,
		236, 236, 237, 237, 237, 238, 238, 238, 239, 239, 239, 239, 240, 240,
		240, 240, 241, 241, 241, 242, 242, 242, 242, 243, 243, 243, 243, 243,
		244, 244, 244, 244, 245, 245, 245, 245, 246, 246, 246, 246, 246, 247,
		247, 247, 247, 247, 248, 248, 248, 248, 248, 249, 249, 249, 249, 249,
		249, 250, 250, 250, 250, 250, 250, 250, 251, 251, 251, 251, 251, 251,
		251, 252, 252, 252, 252, 252, 252, 252, 252, 253, 253, 253, 253, 253,
		253, 253, 253, 253, 253, 254, 254, 254, 254, 254, 254, 254, 254, 254,
		254, 254, 254, 254, 254, 254, 255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254,
		254, 254, 254, 254, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253,
		252, 252, 252, 252, 252, 252, 252, 252, 251, 251, 251, 251, 251, 251,
		251, 250, 250, 250, 250, 250, 250, 250, 249, 249, 249, 249, 249, 249,
		248, 248, 248, 248, 248, 247, 247, 247, 247, 247, 246, 246, 246, 246,
		246, 245, 245, 245, 245, 244, 244, 244, 244, 243, 243, 243, 243, 243,
		242, 242, 242, 242, 241, 241, 241, 240, 240, 240, 240, 239, 239, 239,
		239, 238, 238, 238, 237, 237, 237, 236, 236, 236, 236, 235, 235, 235,
		234, 234, 234, 233, 233, 233, 232, 232, 232, 231, 231, 231, 230, 230,
		230, 229, 229, 229, 228, 228, 228, 227, 227, 226, 226, 226, 225, 225,
		225, 224, 224, 223, 223, 223, 222, 222, 222, 221, 221, 220, 220, 219,
		219, 219, 218, 218, 217, 217, 217, 216, 216, 215, 215, 214, 214, 214,
		213, 213, 212, 212, 211, 211, 210, 210, 210, 209, 209, 208, 208, 207,
		207, 206, 206, 205, 205, 204, 204, 203, 203, 202, 202, 201, 201, 201,
		200, 200, 199, 199, 198, 197, 197, 196, 196, 195, 195, 194, 194, 193,
		193, 192, 192, 191, 191, 190, 190, 189, 189, 188, 188, 187, 186, 186,
		185, 185, 184, 184, 183, 183, 182, 181, 181, 180, 180, 179, 179, 178,
		177, 177, 176, 176, 175, 175, 174, 173, 173, 172, 172, 171, 170, 170,
		169, 169, 168, 167, 167, 166, 166, 165, 164, 164, 163, 163, 162, 161,
		161, 160, 159, 159, 158, 158, 157, 156, 156, 155, 154, 154, 153, 152,
		152, 151, 151, 150, 149, 149, 148, 147, 147, 146, 145, 145, 144, 143,
		143, 142, 141, 141, 140, 139, 139, 138, 137, 137, 136, 135, 135, 134,
		133, 133, 132, 131, 130, 130, 129, 128, 128, 127, 126, 126, 125, 124,
		124, 123, 122, 121, 121, 120, 119, 119, 118, 117, 116, 116, 115, 114,
		114, 113, 112, 111, 111, 110, 109, 109, 108, 107, 106, 106, 105, 104,
		103, 103, 102, 101, 101, 100, 99, 98, 98, 97, 96, 95, 95, 94, 93, 92,
		92, 91, 90, 89, 89, 88, 87, 86, 86, 85, 84, 83, 83, 82, 81, 80, 80, 79,
		78, 77, 77, 76, 75, 74, 73, 73, 72, 71, 70, 70, 69, 68, 67, 67, 66, 65,
		64, 63, 63, 62, 61, 60, 60, 59, 58, 57, 56, 56, 55, 54, 53, 52, 52, 51,
		50, 49, 49, 48, 47, 46, 45, 45, 44, 43, 42, 41, 41, 40, 39, 38, 38, 37,
		36, 35, 34, 34, 33, 32, 31, 30, 30, 29, 28, 27, 26, 26, 25, 24, 23, 22,
		22, 21, 20, 19, 18, 18, 17, 16, 15, 14, 14, 13, 12, 11, 10, 10, 9, 8, 7,
		6, 6, 5, 4, 3, 2, 2, 1, 0 };

#define TABLE_LEN (sizeof(sintable) / sizeof(sintable[0]))

volatile uint8_t gain;

ISR(TIMER1_CAPT_vect) {
	static uint16_t idx;

	static uint8_t b;

	if (!EN) {
		OCR1A = 0;
		OCR1B = TOP;
		idx = 0;
		return;
	}

//	if (b)
//		for (;;)
//			;
//	b = 1;
//	sei();
	uint8_t sin = pgm_read_byte(&sintable[idx++]);
	uint8_t n = sin * gain >> 8;
	OCR1A = n;
	OCR1B = TOP - n;
	if (idx == TABLE_LEN) {
		idx = 0;
		changeSign();
	}
//	_delay_us(2);
//	cli();
//	b = 0;
}

void initHW() {
	DDRB |= (1 << PB1);
	DDRB |= (1 << PB2);

	TCCR1A = (1 << COM1A1) | (1 << COM1B1) | (1 << COM1B0) | (1 << WGM11);
	TCCR1B = (1 << WGM13) | (1 << CS10);

	ICR1H = 0;
	ICR1L = TOP;

	OCR1A = 0;
	OCR1B = TOP;

	TIMSK1 = 0x20;

	configEn();
	configSign();
}

#define ADC_U0 ((uint8_t)(U0 * 255L / 500L))

//#define ADC_U0 159U

int main() {
	initHW();
	initADC();

	sei();
	while (1) {
		adcStep();

//		gain = adc_data[0] * (uint8_t) TOP >> 8;

		unsigned long k2 = 256;
		if (ADC2 > ADC_U0)
			k2 = 256U * ADC_U0 / ADC2;

		gain = (unsigned long) ADC1 * k2 * (unsigned long) TOP >> 16;

	}

}

