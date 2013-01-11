/*****************************************************
This program was produced by the
CodeWizardAVR V1.25.8 Standard
Automatic Program Generator
© Copyright 1998-2007 Pavel Haiduc, HP InfoTech s.r.l.
http://www.hpinfotech.com

Project :
Version :
Date    : 12.02.2009
Author  : F4CG
Company : F4CG
Comments:


Chip type           : ATmega48
Clock frequency     : 8,000000 MHz
Memory model        : Small
External SRAM size  : 0
Data Stack size     : 128
*****************************************************/

#include "common.h"

#ifdef ATMEGA8
#asm
.EQU PINB= 0x16
.EQU DDRB= 0x17
.EQU PORTB= 0x18

.EQU PINC= 0x13
.EQU DDRC= 0x14
.EQU PORTC= 0x15

.EQU PIND= 0x10
.EQU DDRD= 0x11
.EQU PORTD= 0x12
#endasm
#endif

#if defined(ATMEGA48) || defined(ATMEGA168)
#asm
.EQU PINB= 0x3
.EQU DDRB= 0x4
.EQU PORTB= 0x5

.EQU PINC= 0x6
.EQU DDRC= 0x7
.EQU PORTC= 0x8

.EQU PIND= 0x9
.EQU DDRD= 0x0a
.EQU PORTD= 0x0b
#endasm
#endif

#if defined(BOARD1)
#asm
.equ __w1_port=PORTD
.equ __w1_bit=5
#endasm
#endif            

#if defined(BOARD2)
#asm
.equ __w1_port=PORTD
.equ __w1_bit=5
#endasm
#endif


#pragma warn-
char qcli() {
#asm
	IN   R30,SREG
	cli
#endasm
}

void qsei(char sreg_val) {
#asm
	ld  r30, Y
	out   SREG, r30
#endasm
}
#pragma warn+

//extern char adc_chans;

void addFletchSum(unsigned char c, unsigned char* S) {
	*S += c;
	if(*S < c)
	(*S)++;
}

unsigned char fletchSum(unsigned char *buf, unsigned char len){
	unsigned char S = 0;
	for(; len > 0; len--){
		unsigned char R = *buf++;
		S += R;
		if(S < R)
		S++;
	}
	//if(S = 255) S = 0;
	return S;
}


/*
unsigned char Crc8(unsigned char *pcBlock, unsigned char len) {
	unsigned char crc = 0xFF;
	unsigned char i;

	while (len--) {
		crc ^= *pcBlock++;
		for (i = 0; i < 8; i++)
			crc = crc & 0x80 ? (crc << 1) ^ 0x31 : crc << 1;
	}
	return crc;
}

S1 = 0; S2 = 0;
while (<есть данные>)
{
S1 = S1 + <следующий байт>;
S2 = S2 + S1;
}

typedef unsigned char byte;
	byte FletchSum(byte *buf, size_t len){
		byte S = 0;
		for(; len > 0; len--){
			byte R = *buf++;
			S += R; if(S < R) S++;
		}
		//if(S = 255) S = 0;
		return S;
	}

void fletcher16( uint8_t *checkA, uint8_t *checkB, uint8_t *data, size_t len ){
	uint16_t sum1 = 0xff, sum2 = 0xff;
	while (len) {
		size_t tlen = len > 21 ? 21 : len;
		len -= tlen;
		do {
			sum1 += *data++;
			sum2 += sum1;
		} while (--tlen);
		sum1 = (sum1 & 0xff) + (sum1 >> 8);
		sum2 = (sum2 & 0xff) + (sum2 >> 8);
	}
	// Second reduction step to reduce sums to 8 bits
	sum1 = (sum1 & 0xff) + (sum1 >> 8);
	sum2 = (sum2 & 0xff) + (sum2 >> 8);
	*checkA = (uint8_t)sum1;
	*checkB = (uint8_t)sum2;
	return;
}

*/

void print2(flash char* format, int n1, int n2) {
	flash char* pc = format;
	int args[2];
	int idx = 0;
	args[0] = n1;
	args[1] = n2;
	while(*pc) {
		char c = *pc;
		if(c == '%') {
			char buf[10];
			char* pc1 = buf;
			itoa(args[idx++], buf);
			while(*pc1)
				putchar(*(pc1++));
			pc += 2;
		} else {
			putchar(*(pc++));
		}
	}
}               

void print1(flash char* format, int n1) {
	print2(format, n1, 0);
}

void print0(flash char* format) {
	print1(format, 0);
}


void foo() {}
static void handleTimers();
void (*idle)() = foo;

static void usart_init();
static void init_adc(char inputs);

void main(void)
{                                       

#if defined(ATMEGA48) || defined(ATMEGA168)

	// Crystal Oscillator division factor: 1
#pragma optsize-
	CLKPR=0x80;
	CLKPR=0x00;
#ifdef _OPTIMIZE_SIZE_
#pragma optsize+
#endif

#endif
	
	timer0_init();

	// External Interrupt(s) initialization
	// INT0: Off
	// INT1: Off
	// Interrupt on any change on pins PCINT0-7: Off
	// Interrupt on any change on pins PCINT8-14: Off
	// Interrupt on any change on pins PCINT16-23: Off
#if defined(ATMEGA48) || defined(ATMEGA168)
	EICRA=0x00;
	EIMSK=0x00;
	PCICR=0x00;
#else
#ifdef ATMEGA8
	MCUCR=0x00;
#else
#error
#endif
#endif

#ifdef ATMEGA48
	// Timer/Counter 1 Interrupt(s) initialization
	TIMSK1=0x00;
	// Timer/Counter 2 Interrupt(s) initialization
	TIMSK2=0x00;
#endif

	// Analog Comparator initialization
	// Analog Comparator: Off
	// Analog Comparator Input Capture by Timer/Counter 1: Off
#if defined(ATMEGA48) || defined(ATMEGA168)
	ACSR=0x80;
	ADCSRB=0x00;
#else
#ifdef ATMEGA8
	ACSR=0x80;
	SFIOR=0x00;
#else
#error
#endif
#endif

	init_adc(ADC_CHANS);

	usart_init();

	//trace0("init\r\n");

	// Global enable interrupts
#asm("sei")

	createObjects();      
	
	while (1) {
		handleTimers();
		checkPins();
		if(hasMessage()) {
			Message msg;
			getMessage(&msg);
			if(msg.target)
			msg.target->handler(msg.target, &msg);
		} else {
			idle();
/*			
#if defined(ATMEGA48) || defined(ATMEGA168)
			SMCR = 0x01;
			#asm("sleep")
			#asm("nop")
			SMCR = 0x00;
#else
#ifdef ATMEGA8
			MCUCR |= 0x80;
			#asm("sleep")
			#asm("nop")
			MCUCR &= ~0x80;
#else
#error
#endif
#endif
*/			
		}
	}
}

// ====================================== MESSAGES ========================================================
static Message msgQueue[MSG_QUEUE_SIZE];
static char msgCnt;
static char msgWrIdx;
static char msgRdIdx;

int sendMessage0(char cmd, struct _ObjectHeader* target) {
	return sendMessage(cmd, target, 0);
}

void postMessage0(char cmd, struct _ObjectHeader* target) {
	postMessage(cmd, target, 0);
}

int sendMessage(char cmd, struct _ObjectHeader* target, int data) {
	Message msg;
	msg.cmd = cmd;
	msg.target = target;
	msg.data = data;
	return target->handler(target, &msg);
}

void postMessage(char cmd, struct _ObjectHeader* target, int data) {
	char msgOvf = 0;
	char sreg_val = qcli();

	if(msgCnt == MSG_QUEUE_SIZE) {
		msgOvf = 1;
	} else {
		Message* msg = &msgQueue[msgWrIdx];
		msg->cmd = cmd;
		msg->target = target;
		msg->data = data;
		if (++msgWrIdx == MSG_QUEUE_SIZE)
		msgWrIdx=0;
		++msgCnt;
	}

	qsei(sreg_val);

	if(msgOvf)
	trace1("msg buf ovf %d\r\n", cmd);
}

char hasMessage() {
	return msgCnt != 0;
}

void getMessage(Message* msg) {
	char sreg_val;

	while(msgCnt == 0);
	*msg = msgQueue[msgRdIdx];
	if(++msgRdIdx == MSG_QUEUE_SIZE)
	msgRdIdx = 0;

	sreg_val = qcli();
	msgCnt--;
	qsei(sreg_val);
}  
/*
void removeMessage(char cmd, struct _ObjectHeader* target) {
	char sreg_val = qcli();
	char i = msgRdIdx;
	char cnt = msgCnt;
	while(cnt--) {
		Message* msg;
		if(++i == MSG_QUEUE_SIZE)
			i = 0;
		msg = &msgQueue[i];
		if(msg->cmd == cmd && msg->target == target)
			msg->target = 0;
	}
	qsei(sreg_val);
}
*/

//========================================== TIMER0 ==================================================

typedef struct {
	char cmd;
	ObjectHeader* obj;
	char periodic;
	long period;
	long time;
	int param;
} TIMER;

static TIMER timers[NTIMERS];

void (*ledHandler)() = foo;

#ifdef SINGLE_MILLIS
void millis();
#else
void (*millis)() = foo;
#endif

static long time;

long getTimeMillisCli() {
	return time;
}

long getTimeMillis() {
	long res;
	#asm("cli");
	res = time;
	#asm("sei");
	return res;
}

static char timerTicks; 

interrupt [TIM0_OVF] void timer0_ovf_isr(void)
{
	static char inHandler;
	static char n = 10;

	TCNT0=255-31;

	time++;

	millis();

	if(!(--n)) {
		n = 10;
		if(!inHandler) {
			inHandler = 1;
			timerTicks++;            
			#asm("sei");
			ledHandler();
			#asm("cli");
			inHandler = 0;
		}
	}
}

static void handleTimers() {
	char tticks;
	#asm("cli");
	tticks = timerTicks;
	timerTicks = 0;
	#asm("sei");
	while(tticks) {
		char i;
		tticks--;
		for(i = 0; i < NTIMERS; i++) {
			TIMER* timer = &timers[i];
			if(timer->obj) {
				timer->time -= 10;
				if(timer->time <= 0) {
					char cmd = timer->cmd;
					ObjectHeader* obj = timer->obj;
					int param = timer->param;
					if(timer->periodic)
					    timer->time += timer->period;
					else
					    timer->obj = 0;
					sendMessage(cmd, obj, param);
				}
			}
		}
	}
}

void setTimerParam(ObjectHeader* obj, char cmd, char type, long ms, int param) {
	char i;
	TIMER* freeTimer = 0; 
	
	char periodic = (type == TIMER_PERIODIC);
	long ms1 = ms;
	
	for(i = 0; i < NTIMERS; i++) {
		TIMER* timer = &timers[i];
		long time = timer->time;
		if(timer->obj == obj && timer->cmd == cmd) {
			if((type == TIMER_MAX && time > ms) || (type == TIMER_MIN && time < ms))
			ms1 = time;
			freeTimer = timer;
			break;
		} else if(!timer->obj) {
			freeTimer = timer;
		}
	}

	if(freeTimer) {
		freeTimer->obj = obj;
		freeTimer->cmd = cmd;
		freeTimer->time = freeTimer->period = ms1;
		freeTimer->periodic = periodic;
		freeTimer->param = param;
	}
	if(!freeTimer)
	trace0("no free timers");
}

void setTimer(ObjectHeader* obj, char cmd, char type, long ms) {
	setTimerParam(obj, cmd, type, ms, 0);
}

void killTimer(ObjectHeader* obj, char cmd) {
	char i;
	for(i = 0; i < NTIMERS; i++) {
		TIMER* timer = &timers[i];
		if(timer->obj == obj && timer->cmd == cmd) {
			timer->obj = 0;
			break;
		}
	}
}

void timer0_init(void)
{
	// Timer/Counter 0 initialization
	// Clock source: System Clock
	// Clock value: 7,813 kHz
	// Mode: Normal top=FFh
	// OC0A output: Disconnected
	// OC0B output: Disconnected

#if defined(ATMEGA48) || defined(ATMEGA168)

	TCCR0A=0x00;
	TCCR0B=0x04;
	TCNT0=255-31;
	OCR0A=0x00;
	OCR0B=0x00;
	TIMSK0=0x01;

#else
#ifdef ATMEGA8

	TCCR0 = 0x04;
	TCNT0 = 255-31;
	TIMSK |= 0x01;

#else
#error	
#endif
#endif
}

//========================================= USART =============================================

#define BAUD 9600

#define RXB8 1
#define TXB8 0
#define UPE 2
#define OVR 3
#define FE 4
#define UDRE 5
#define RXC 7

#define FRAMING_ERROR (1<<FE)
#define PARITY_ERROR (1<<UPE)
#define DATA_OVERRUN (1<<OVR)
#define DATA_REGISTER_EMPTY (1<<UDRE)
#define RX_COMPLETE (1<<RXC)

#if defined(ATMEGA48) || defined(ATMEGA168)
#define UCSRA UCSR0A
#define UDR UDR0
#define UCSRB UCSR0B
#define UCSRC UCSR0C
#define UBRRH UBRR0H
#define UBRRL UBRR0L
#endif
/*
static ObjectHeader* usartListener;

void setUsartListener(ObjectHeader* l) {
	usartListener = l;
}
*/
void handleRxCmd(char* cmd);

#define RXBUFSIZE 16

interrupt [USART_RXC] void usart_rx_isr(void)
{
	static char rxBuf1[RXBUFSIZE];
	static char rxBuf2[RXBUFSIZE];
	
	static unsigned char rxIdx;
	static char* buf = rxBuf1;
	static char* oldBuf = rxBuf2;
	
	char status = UCSRA;
	char data = UDR;

#ifdef BINARY_DATA

	static long lastTime;

	if ((status & (FRAMING_ERROR | PARITY_ERROR | DATA_OVERRUN))==0)
	{
		long time = getTimeMillisCli();
		if(time - lastTime > 2)
		rxIdx = 0;
		lastTime = time;            

		//
		
		buf[rxIdx] = data;
		if(rxIdx < RXBUFSIZE - 1)
		rxIdx++;
		
		if(rxIdx == buf[0]) {
			char* temp = buf;
			buf = oldBuf;
			oldBuf = temp;
			rxIdx = 0;
			#asm("sei");
			handleRxCmd(oldBuf);
			#asm("cli");
		}
	}
#else
	if ((status & (FRAMING_ERROR | PARITY_ERROR | DATA_OVERRUN))==0)
	{
		if(data < ' ') {
			if(rxIdx > 0) {
				char* temp = buf;
				buf = oldBuf;
				oldBuf = temp;
				oldBuf[rxIdx] = '\0';
				rxIdx = 0;
				#asm("sei");
				handleRxCmd(oldBuf);
				#asm("cli");
			}
		} else {
			buf[rxIdx] = data;
			if(rxIdx < RXBUFSIZE - 1)
			rxIdx++;
		}
	}
#endif    
}

// USART Transmitter buffer

#ifdef ATMEGA8
#define TX_BUFFER_SIZE 64
#endif

#ifdef ATMEGA48
#define TX_BUFFER_SIZE 64
#endif

#ifdef ATMEGA168
#define TX_BUFFER_SIZE 64
#endif


static char tx_buffer[TX_BUFFER_SIZE];

#if TX_BUFFER_SIZE<256
unsigned char tx_wr_index,tx_rd_index,tx_counter;
#else
unsigned int tx_wr_index,tx_rd_index,tx_counter;
#endif

// USART Transmitter interrupt service routine
interrupt [USART_TXC] void usart_tx_isr(void) {
	if (tx_counter) {
		--tx_counter;
		UDR=tx_buffer[tx_rd_index];
		if (++tx_rd_index == TX_BUFFER_SIZE)
		tx_rd_index=0;
	} else {
		TX_END();
	}
}

#ifndef _DEBUG_TERMINAL_IO_
// Write a character to the USART Transmitter buffer
#define _ALTERNATE_PUTCHAR_
#pragma used+
void putchar(char c) {
	char sreg;
	while (tx_counter == TX_BUFFER_SIZE);
	sreg = qcli();
	TX_START();
	if (tx_counter || ((UCSRA & DATA_REGISTER_EMPTY)==0)) {
		tx_buffer[tx_wr_index]=c;
		if (++tx_wr_index == TX_BUFFER_SIZE)
		tx_wr_index=0;
		++tx_counter;
	} else
	UDR=c;
	qsei(sreg);
}
#pragma used-
#endif

static void usart_init() {
	// USART initialization
	// Communication Parameters: 8 Data, 1 Stop, No Parity
	// USART Receiver: On
	// USART Transmitter: On
	// USART0 Mode: Asynchronous
	// USART Baud Rate: 9600
	UCSRA=0x00;
	UCSRB=0xD8;
#if defined(ATMEGA48) || defined(ATMEGA168)
	UCSRC=0x06;
#else
#ifdef ATMEGA8
	UCSRC=0x86;
#else
#error
#endif
#endif

#define MYUBRR ((FOSC)/16/(BAUD)-1)
	UBRRH = (unsigned char)(MYUBRR >> 8);
	UBRRL = (unsigned char)MYUBRR;
}

//=============================================== ADC ===========================================

#include <delay.h>

#ifdef INT_REF
#define ADC_VREF_TYPE 0xC0
#else
#ifdef AVCC_REF
#define ADC_VREF_TYPE 0x40
#else
#error
#endif
#endif

// Read the AD conversion result
unsigned int read_adc(unsigned char adc_input)
{
	int res;
	char sreg = qcli();

	ADMUX=adc_input | (ADC_VREF_TYPE & 0xff);
	// Delay needed for the stabilization of the ADC input voltage
	delay_us(10);
	// Start the AD conversion
	ADCSRA|=0x40;
	// Wait for the AD conversion to complete
	while ((ADCSRA & 0x10)==0);
	ADCSRA|=0x10;

	res = ADCW;
	qsei(sreg);

	return res;
}

#pragma warn-
static void init_adc(char inputs) {
	// ADC initialization
	// ADC Clock frequency: 1000,000 kHz
	// ADC Voltage Reference: AVCC pin
	// ADC Auto Trigger Source: None
#if defined(ATMEGA48) || defined(ATMEGA168)
	DIDR0=inputs;
#endif
	ADMUX=ADC_VREF_TYPE & 0xff;
	ADCSRA=0x83;
}
#pragma warn+

//========================================== LED ======================================================


static char ledState[6];

extern void (*ledHandler)();
static void ledStep();

void setLed(char led, char s) {
	LED_GRP0_DDR = 1;
	LED_GRP1_DDR = 1;
	LED0_DDR = 1;
	LED1_DDR = 1;
	LED2_DDR = 1;
	
	#asm  ("cli");
	ledHandler = ledStep;
	#asm  ("sei");

	if(led < sizeof(ledState) / sizeof(ledState[0]))
	ledState[led] = s;
}

char getLed(char led) {
	if(led < sizeof(ledState) / sizeof(ledState[0]))
	return ledState[led];
	return 0;
}

static void ledStep() {
	static char g;
	static char step;
	char* s;
	char st;

	step++;
	if(step == 100)
	step = 0;

	LED0 = 0;
	LED1 = 0;
	LED2 = 0;

	LED_GRP0 = 1;
	LED_GRP1 = 1;

	g = g ^ 1;

	if(g)
	LED_GRP1 = 0;
	else
	LED_GRP0 = 0;

#pragma warn-
	s = ledState + (g + g + g);
#pragma warn+

	st = *(s++);
	if(st == 0)
	LED0 = 0;
	else if(st == 1)
	LED0 = 1;
	else if(step < 50)
	LED0 = 0;
	else
	LED0 = 1;

	st = *(s++);
	if(st == 0)
	LED1 = 0;
	else if(st == 1)
	LED1 = 1;
	else if(step < 50)
	LED1 = 0;
	else
	LED1 = 1;

	st = *s;
	if(st == 0)
	LED2 = 0;
	else if(st == 1)
	LED2 = 1;
	else if(step < 50)
	LED2 = 0;
	else
	LED2 = 1;

}

//======================================= PIN ================================================

#define NEWPIN 1
#if NEWPIN

#asm

convertPortId:
// r30-portpin -> r26-port, r27-mask
MOV R26, R30
LDI R27, 1
ANDI R30, 0x0F
BREQ convertPortIdR
convertPortIdL:
LSL  R27
DEC  R30
BRNE convertPortIdL
convertPortIdR:
LSR R26
LSR R26
LSR R26
LSR R26
RET


inPinTable:
IN R30, PINB
RET
IN R30, PINC
RET
IN R30, PIND
RET
#ifdef ATMEGA32
IN R30, PINA
RET
#endif
inPortTable:
IN R30, PORTB
RET
IN R30, PORTC
RET
IN R30, PORTD
RET
#ifdef ATMEGA32
IN R30, PORTA
RET
#endif
outPortTable:
OUT PORTB, R26
RET
OUT PORTC, R26
RET
OUT PORTD, R26
RET
#ifdef ATMEGA32
OUT PORTA, R26
RET
#endif
inDDRTable:
IN R30, DDRB
RET
IN R30, DDRC
RET
IN R30, DDRD
RET
#ifdef ATMEGA32
IN R30, DDRA
RET
#endif
outDDRTable:
OUT DDRB, R26
RET
OUT DDRC, R26
RET
OUT DDRD, R26
RET
#ifdef ATMEGA32
OUT DDRA, R26
RET
#endif
#endasm

#pragma warn-
static char getPinPort(char portPin, char offset /* 0 or 8 */) {
#asm
	LDD R30, Y+1
	RCALL convertPortId

	ADD R26, R26
	LD r30, Y
	ADD r30, r26
	LDI R31, 0
	SUBI R30, LOW(-inPinTable)
	SBCI R31, HIGH(-inPinTable)
	ICALL

	AND R30, r27
#endasm
}

static void setPortDDR(char portPin, char val, char offset /* 0 or 16 */) {
#asm
	LDD R30, Y+2
	RCALL convertPortId

	mov r30, r26

	ADD R30, R30
	LD R31, Y
	ADD R30, r31
	LDI R31, 0
	SUBI R30, LOW(-inPortTable)
	SBCI R31, HIGH(-inPortTable)
	ICALL

	LDD R31, Y+1
	tst r31
	breq setport1
	OR R27, r30
	rjmp setport2
setport1:
	COM r27
	AND r27, r30
setport2:

	mov r30, r26
	mov r26, r27

	ADD R30, R30
	LD R31, Y
	ADD R30, r31
	LDI R31, 0
	SUBI R30, LOW(-outPortTable)
	SBCI R31, HIGH(-outPortTable)
	ICALL
#endasm
}

#pragma warn+

char getPin(char portPin) {
	if(portPin == NO_PIN)
	return 0;
	return getPinPort(portPin, 0);
}

char getPort(char portPin) {
	if(portPin == NO_PIN)
	return 0;
#ifdef ATMEGA32
	return getPinPort(portPin, 8);
#else
	return getPinPort(portPin, 6);
#endif
}

void setPort(char portPin, char val) {
	if(portPin == NO_PIN)
	return;
	setPortDDR(portPin, val, 0);
}

void confPin(char portPin, char dir, char pullUp) {
	if(portPin == NO_PIN)
	return;
#ifdef ATMEGA32
	setPortDDR(portPin, dir, 16);
#else
	setPortDDR(portPin, dir, 12);
#endif
	setPortDDR(portPin, pullUp, 0);
}

#else

static char inPin(char port) {
#ifdef ATMEGA32
	if(port == PA)
	return PINA;
#endif
	if(port == PB)
	return PINB;
	if(port == PC)
	return PINC;
	if(port == PD)
	return PIND;
}

static char inPort(char port) {
#ifdef ATMEGA32
	if(port == PA)
	return PORTA;
#endif
	if(port == PB)
	return PORTB;
	if(port == PC)
	return PORTC;
	if(port == PD)
	return PORTD;
}

static char inDDR(char port) {
#ifdef ATMEGA32
	if(port == PA)
	return DDRA;
#endif
	if(port == PB)
	return DDRB;
	if(port == PC)
	return DDRC;
	if(port == PD)
	return DDRD;
}

static void outPort(char port, char b) {
#ifdef ATMEGA32
	if(port == PA)
	PORTA = b;
	else
#endif
	if(port == PB)
	PORTB = b;
	else if(port == PC)
	PORTC = b;
	else if(port == PD)
	PORTD = b;
}

static void outDDR(char port, char b) {
#ifdef ATMEGA32
	if(port == PA)
	DDRA = b;
	else
#endif
	if(port == PB)
	DDRB = b;
	else if(port == PC)
	DDRC = b;
	else if(port == PD)
	DDRD = b;
}


void setPort(char portPin, char val) {
	char v;
	char port;
	char mask;
	if(portPin == NO_PIN)
	return;
	port = portPin & 0xF0;
	mask = 1 << (portPin & 0x0F);

	v = inPort(port);
	if(val)
	v |= mask;
	else
	v &= ~mask;
	outPort(port, v);
}

void confPin(char portPin, char dir, char pullUp) {
	char v;
	char port;
	char mask;
	if(portPin == NO_PIN)
	return;
	port = portPin & 0xF0;
	mask = 1 << (portPin & 0x0F);

	v = inDDR(port);
	if(dir)
	v |= mask;
	else
	v &= ~mask;
	outDDR(port, v);

	setPort(portPin, pullUp);
}

char getPort(char portPin) {
	char port;
	char mask;
	if(portPin == NO_PIN)
	return 0;
	port = portPin & 0xF0;
	mask = 1 << (portPin & 0x0F);
	return inPort(port) & mask;
}

char getPin(char portPin) {
	char port;
	char mask;
	if(portPin == NO_PIN)
	return 0;
	port = portPin & 0xF0;
	mask = 1 << (portPin & 0x0F);
	return inPin(port) & mask;
}

#endif

#define NPINS 4

typedef struct {
	char portPin;
	char cmd;
	ObjectHeader* obj;
	char state;
} PIN;

static PIN pins [NPINS];

void checkPins() {
	char i;
	for(i = 0; i < NPINS; i++) {
		PIN* pin = &pins[i];
		if(pin->obj) {
			char st = getPin(pin->portPin);
			if(st != pin->state) {
				sendMessage(pin->cmd, pin->obj, st);
				pin->state = st;
			}
		}
	}
}

void setPinListener(char portPin, ObjectHeader* obj, char cmd) {

	char i;
	PIN* freePin = 0;
	
	if(portPin == NO_PIN)
	    return;
	    
	for(i = 0; i < NPINS; i++) {
		PIN* pin = &pins[i];
		if(pin->portPin == portPin) {
			freePin = pin;
			break;
		} else if(!pin->obj) {
			freePin = pin;
		}
	}
	if(freePin) {
		freePin->cmd = cmd;
		freePin->obj = obj;
		freePin->portPin = portPin;
		freePin->state = getPin(portPin);
	}
	if(!freePin)
	    trace0("no free pins\r\n");
}


