
#include <mega8.h>

#define getInput() (PIND.0)

#define setOutput(b) (PORTD.1 = (b))

#define MAX_IN_CNT 0xE600
                         
#define SHR5(x) do { (x)>>=1; (x)>>=1; (x)>>=1; (x)>>=1; (x)>>=1; } while(0)

#define set1(b) (PORTB.7=(b))
#define set2(b) (PORTB.1=(b))

// Timer 0 overflow interrupt service routine
interrupt [TIM0_OVF] void timer0_ovf_isr(void)
{
    static unsigned int inCnt;
    static unsigned int outCnt;
    static unsigned int outT;
    static unsigned int outT1;

    static char prevInput;

    char input;
                                 
    TCNT0 = 0xFF - 20;
    
    PORTD.2 = 1;
               
    outCnt++;
    if(outCnt >= outT) {
        setOutput(0);
        set2(0);
        outCnt = 0;
    } else if(outCnt >= outT1) {
        setOutput(1);
        set2(1);
    }          
    
    if(inCnt != MAX_IN_CNT)
		inCnt++;
		
	input = getInput();   
	set1(input);
	
    if(inCnt == MAX_IN_CNT)
    	outT1 = outT = 0;
	
	if(input && !prevInput) {
        if(inCnt != MAX_IN_CNT) {
            outT = inCnt;
            SHR5(inCnt);
            outT += inCnt;
            inCnt >>= 1;
            outT += inCnt;
            inCnt >>= 1;
            outT += inCnt;
            inCnt >>= 1;
            outT += inCnt;
	    }	
    	outT1 = outT >> 1;
	    inCnt = 0;
    }
    prevInput = input;
    
    PORTD.2 = 0;
    
//    outT = inCnt + (((long)inCnt * 14) >> 8);
}

void main(void)
{
// Declare your local variables here

// Input/Output Ports initialization
// Port B initialization
// Func7=In Func6=In Func5=In Func4=In Func3=In Func2=In Func1=In Func0=In 
// State7=T State6=T State5=T State4=T State3=T State2=T State1=T State0=T 
PORTB=0x00;
DDRB=0x82;

// Port C initialization
// Func6=In Func5=In Func4=In Func3=In Func2=In Func1=In Func0=In 
// State6=T State5=T State4=T State3=T State2=T State1=T State0=T 
PORTC=0x00;
DDRC=0x01;

// Port D initialization
// Func7=In Func6=In Func5=In Func4=In Func3=In Func2=In Func1=Out Func0=In 
// State7=T State6=T State5=T State4=T State3=T State2=T State1=0 State0=T 
PORTD=0x00;
DDRD=0x02;

// Timer/Counter 0 initialization
// Clock source: System Clock
// Clock value: 1 MHz
TCCR0=0x02;
TCNT0=0x10;

// Timer/Counter 1 initialization
// Clock source: System Clock
// Clock value: Timer 1 Stopped
// Mode: Normal top=FFFFh
// OC1A output: Discon.
// OC1B output: Discon.
// Noise Canceler: Off
// Input Capture on Falling Edge
// Timer 1 Overflow Interrupt: Off
// Input Capture Interrupt: Off
// Compare A Match Interrupt: Off
// Compare B Match Interrupt: Off
TCCR1A=0x00;
TCCR1B=0x00;
TCNT1H=0x00;
TCNT1L=0x00;
ICR1H=0x00;
ICR1L=0x00;
OCR1AH=0x00;
OCR1AL=0x00;
OCR1BH=0x00;
OCR1BL=0x00;

// Timer/Counter 2 initialization
// Clock source: System Clock
// Clock value: Timer 2 Stopped
// Mode: Normal top=FFh
// OC2 output: Disconnected
ASSR=0x00;
TCCR2=0x00;
TCNT2=0x00;
OCR2=0x00;

// External Interrupt(s) initialization
// INT0: Off
// INT1: Off
MCUCR=0x00;

// Timer(s)/Counter(s) Interrupt(s) initialization
TIMSK=0x01;

// Analog Comparator initialization
// Analog Comparator: Off
// Analog Comparator Input Capture by Timer/Counter 1: Off
ACSR=0x80;
SFIOR=0x00;

// Global enable interrupts
#asm("sei")
         
    set1(0);
    set2(1);

while (1)
      {
      // Place your code here

      };
}
