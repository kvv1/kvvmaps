/*
 * dht11.h
 *
 *  Created on: 28.07.2013
 *      Author: kvv
 */

#ifndef DHT11_H_
#define DHT11_H_

#include "stdint.h"

typedef enum {
	DHT11, DHT22,
} DHT_TYPE;

int8_t dht_read(DHT_TYPE type, int* t, int* h);

#endif /* DHT11_H_ */
