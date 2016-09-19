echo off
set /a i=0

:l
echo %i%

call make.bat atmega8 %i%

set /a i=%i%+1
if %i% neq 50 goto l

