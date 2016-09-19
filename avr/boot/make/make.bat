@path=%path%;F:\WinAVR-20100110\bin

@set myaddr=%2
@set mcu=%1

if %mcu%==atmega8 set bootaddr=0x1C00
if %mcu%==atmega168 set bootaddr=0x3C00

@set CFLAGS=-Wall -Os -finline-limit=3 -fno-inline-small-functions -fpack-struct -fshort-enums -fdata-sections -ffunction-sections --param inline-call-cost=2 -std=gnu99 -funsigned-char -funsigned-bitfields -mmcu=%mcu% -DF_CPU=8000000UL -MMD -MP -DMYADDR=%myaddr%
@set LFLAGS=-Wl,-Map,boot.map -Wl,--relax,--section-start=.text=%bootaddr% -nostartfiles -nodefaultlibs -mmcu=%mcu%
 
avr-gcc %CFLAGS% -c "../utils.c"
avr-gcc %CFLAGS% -c "../hw.c"
avr-gcc %CFLAGS% -c "../main.c"
avr-gcc %CFLAGS% -c "../packet.c"

avr-gcc %LFLAGS% -o "boot.elf"  ./hw.o ./main.o ./packet.o ./utils.o

avr-objdump -h -S boot.elf  >"boot.lss"
avr-objcopy -R .eeprom -O ihex boot.elf  "hex\boot%myaddr%.hex" 
avr-size --format=avr --mcu=%mcu% boot.elf

