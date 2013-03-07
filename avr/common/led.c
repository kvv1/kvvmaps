//========================================== LED ======================================================
/*
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

 if (led < sizeof(ledState) / sizeof(ledState[0]))
 ledState[led] = s;
 }

 char getLed(char led) {
 if (led < sizeof(ledState) / sizeof(ledState[0]))
 return ledState[led];
 return 0;
 }

 static void ledStep() {
 static char g;
 static char step;
 char* s;
 char st;

 step++;
 if (step == 100)
 step = 0;

 LED0 = 0;
 LED1 = 0;
 LED2 = 0;

 LED_GRP0 = 1;
 LED_GRP1 = 1;

 g = g ^ 1;

 if (g)
 LED_GRP1 = 0;
 else
 LED_GRP0 = 0;

 #pragma warn-
 s = ledState + (g + g + g);
 #pragma warn+

 st = *(s++);
 if (st == 0)
 LED0 = 0;
 else if (st == 1)
 LED0 = 1;
 else if (step < 50)
 LED0 = 0;
 else
 LED0 = 1;

 st = *(s++);
 if (st == 0)
 LED1 = 0;
 else if (st == 1)
 LED1 = 1;
 else if (step < 50)
 LED1 = 0;
 else
 LED1 = 1;

 st = *s;
 if (st == 0)
 LED2 = 0;
 else if (st == 1)
 LED2 = 1;
 else if (step < 50)
 LED2 = 0;
 else
 LED2 = 1;

 }
 */
