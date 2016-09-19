@echo off

set myaddr=%1
set mcu=atmega8

if %mcu%==atmega8 avrdude -pm8 -cavr910 -PCOM3 -u -Uflash:w:hex\boot%myaddr%.hex:a -Ulfuse:w:0x24:m -Uhfuse:w:0xda:m
if %mcu%==atmega168 avrdude -pm168 -cavr910 -PCOM3 -u -Uflash:w:hex\boot%myaddr%.hex:a -Ulfuse:w:0xe2:m -Uhfuse:w:0xdc:m -Uefuse:w:0x02:m
