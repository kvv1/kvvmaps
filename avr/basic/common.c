#include <avr/io.h>

void chipInit() {
#if defined(__AVR_ATmega48__) || defined(__AVR_ATmega168__)

	// Crystal Oscillator division factor: 1
#pragma optsize-
	CLKPR=0x80;
	CLKPR=0x00;
#ifdef _OPTIMIZE_SIZE_
#pragma optsize+
#endif

#endif

	// External Interrupt(s) initialization
	// INT0: Off
	// INT1: Off
	// Interrupt on any change on pins PCINT0-7: Off
	// Interrupt on any change on pins PCINT8-14: Off
	// Interrupt on any change on pins PCINT16-23: Off
#if defined(__AVR_ATmega48__) || defined(__AVR_ATmega168__)
	EICRA=0x00;
	EIMSK=0x00;
	PCICR=0x00;
#else
#ifdef __AVR_ATmega8__
	MCUCR = 0x00;
#else
#error
#endif
#endif

#if defined(__AVR_ATmega48__) || defined(__AVR_ATmega168__)
	// Timer/Counter 1 Interrupt(s) initialization
	TIMSK1=0x00;
	// Timer/Counter 2 Interrupt(s) initialization
	TIMSK2=0x00;
#endif

	// Analog Comparator initialization
	// Analog Comparator: Off
	// Analog Comparator Input Capture by Timer/Counter 1: Off
#if defined(__AVR_ATmega48__) || defined(__AVR_ATmega168__)
	ACSR=0x80;
	ADCSRB=0x00;
#else
#ifdef __AVR_ATmega8__
	ACSR = 0x80;
	SFIOR = 0x00;
#else
#error
#endif
#endif
}

/*
static int commonMain(void) {
	ee_magic = MAGIC16;

	chipInit();

	//	set_sleep_mode(SLEEP_MODE_PWR_DOWN);
//	sleep_enable();
//	sleep_cpu();
//	sleep_disable();

	timer0_init();
	//init_adc(ADC_CHANS, INT_VREF_TYPE);
	init_adc(ADC_CHANS, AVCC_VREF_TYPE);
	uart_init();
	w1_init();

	sei();

	trace0("init\r\n");
	createObjects();

	while (1) {
		char tticks = getClearTimerTicks();
		while (tticks) {
			tticks--;
			ds18b20_step(0, TIME_UNIT);
			ds18b20_step(1, TIME_UNIT);
			vmStep(TIME_UNIT);
			handleTimers(TIME_UNIT);
			handlePWM(TIME_UNIT);
		}
		handleIO();
		handlePins(); handleMessages();
//		if (getMessage(&msg)) {
//			if (msg.target)
//				msg.target->handler(&msg);
//		} else {
//			set_sleep_mode(SLEEP_MODE_IDLE);
//			sleep_enable();
//			sleep_cpu();
//			sleep_disable();
//		}
	}
	return 0;
}
*/
