#include "dht11.h"

#include <util/delay.h>
#include <util/atomic.h>
#include "myio.h"
#include "settings.h"

#define W1_IN()	getPin(PIN_1W)
#define W1_HIGH() (setDDR(PIN_1W, 0), setPort(PIN_1W, 1))
#define W1_LOW() (setDDR(PIN_1W, 1), setPort(PIN_1W, 0))

uint8_t data[5];

int8_t wait(int8_t val, uint8_t timeoutUS) {
	while (--timeoutUS && W1_IN() != val)
		_delay_us(1);
	return timeoutUS;
}

int8_t dht_read(DHT_TYPE type, int* t, int* h) {

	for (int i = 0; i < 5; i++)
		data[i] = 0;

	W1_LOW();
	_delay_ms(25);
	W1_HIGH();
	_delay_us(40);
	if (W1_IN())
		return 1;
	_delay_us(80);
	if (!W1_IN())
		return 2;

	if (!wait(0, 255))
		return 3;

	int idx = 0;
	int cnt = 0;
	uint8_t tmp = 0;

	while (cnt < 40) {
		if (!wait(1, 255))
			return 3;
		_delay_us(30);
		tmp = (tmp << 1);
		if (W1_IN()) {
			tmp |= 1;
			if (!wait(0, 255))
				return 4;
		}
		cnt++;
		if (!(cnt & 7))
			data[idx++] = tmp;
	}

	if ((uint8_t) (data[0] + data[1] + data[2] + data[3]) != data[4])
		return 5;

	if (type == DHT11) {
		*t = data[2];
		*h = data[0];
	} else if (type == DHT22) {
		*t = ((data[2] << 8) | data[3]) / 10;
		*h = ((data[0] << 8) | data[1]) / 10;
	} else {
		return 6;
	}

	for (int i = 0; i < 5; i++)
		print1("%d ", data[i]);
	print2("    %d %d", ((data[0] << 8) + data[1]) / 10,
			((data[2] << 8) + data[3]) / 10);
	print0("\n");

	return 0;
}

