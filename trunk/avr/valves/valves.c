#include "common.h"
#include "packet.h"
#include "commands.h"
#include "valves.h"
#include "vm.h"
#include "settings.h"
#include "timer0.h"
#include "adc.h"
#include "myio.h"
#include "1w.h"


#include <util/delay.h>
#include <avr/interrupt.h>

void packetReceived(uint8_t* data, uint8_t len) {
	ee_magic = MAGIC16;
	handleCmd(data, len);
	ee_magic = 0;
}

int main() {
	chipInit();

	timer0_init();
	//init_adc(ADC_CHANS, INT_VREF_TYPE);
	init_adc(0, AVCC_VREF_TYPE);
	uart_init();
	w1_init();

	sei();

	ee_magic = MAGIC16;
	initCommands();
	initVM();
	ee_magic = 0;

	while (1) {
		char tticks = getClearTimerTicks();
		while (tticks) {
			tticks--;
			ds18b20_step(0, TIME_UNIT);
			ds18b20_step(1, TIME_UNIT);
			vmStep(TIME_UNIT);
			handlePWM(TIME_UNIT);
		}
		handleIO();
	}
	return 0;
}

