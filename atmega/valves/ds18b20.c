/*
CodeVisionAVR C Compiler
(C) 1998-2007 Pavel Haiduc, HP InfoTech S.R.L.

Dallas Semiconductor DS18B20 1 Wire bus temperature sensor functions
*/

#include <delay.h>
#include "ds18b20.h"

struct __ds18b20_scratch_pad_struct __ds18b20_scratch_pad;

// temp. conversion time [ms] depending on the resolution
static flash int conv_delay[4]={100,200,400,800};
// valid temp. bit mask depending on the resolution
static flash unsigned bit_mask[4]={0xFFF8,0xFFFC,0xFFFE,0xFFFF};

/*
unsigned char ds18b20_select(unsigned char *addr)
{
unsigned char i;
if (_w1_init()==0) return 0;
if (addr)
{
_w1_write(0x55);
i=0;
do {
	_w1_write(*(addr++));
}
while (++i<8);
}
else _w1_write(0xcc);
return 1;
}
*/

unsigned char ds18b20_read_spd(unsigned char *addr)
{
	unsigned char i;
	unsigned char *p;
	if (_w1_select(addr)==0) return 0;
	_w1_write(0xbe);
	i=0;
	p=(char *) &__ds18b20_scratch_pad;
	do
	*(p++)=_w1_read();
	while (++i<9);
	return !_w1_dow_crc8(&__ds18b20_scratch_pad,9);
}

float ds18b20_temperature(unsigned char *addr)
{
	unsigned char resolution;
	if (ds18b20_read_spd(addr)==0) return -9999;
	resolution=(__ds18b20_scratch_pad.conf_register>>5) & 3;
	if (_w1_select(addr)==0) return -9999;
	_w1_write(0x44);
	delay_ms(conv_delay[resolution]);
	if (ds18b20_read_spd(addr)==0) return -9999;
	_w1_init();
	return (*((int *) &__ds18b20_scratch_pad.temp_lsb) & ((int) bit_mask[resolution]))*0.0625;
}

unsigned char ds18b20_init(unsigned char *addr,signed char temp_low,signed char temp_high,
unsigned char resolution)
{
	if (_w1_select(addr)==0) return 0;
	resolution=(resolution<<5) | 0x1f;
	_w1_write(0x4e);
	_w1_write(temp_high);
	_w1_write(temp_low);
	_w1_write(resolution);
	if (ds18b20_read_spd(addr)==0) return 0;
	if ((__ds18b20_scratch_pad.temp_low!=temp_low) ||
			(__ds18b20_scratch_pad.temp_high!=temp_high) ||
			(__ds18b20_scratch_pad.conf_register!=resolution)) return 0;
	if (_w1_select(addr)==0) return 0;
	_w1_write(0x48);
	delay_ms(15);
	return _w1_init();
}
