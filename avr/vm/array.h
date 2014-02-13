#ifndef _ARRAY_H_
#define _ARRAY_H_

#include <stdint.h>

typedef struct {
	int a;
	int sz;
	int objArr;
} Array;

void array_init(Array* arr, int objArr);
int array_size(Array* arr);
uint16_t array_getAt(Array* arr, int idx);
void array_setAt(Array* arr, int idx, uint16_t val);
int array_add(Array* arr, uint16_t val);
void array_compact(Array* arr);
void array_clear(Array* arr, uint16_t val);

#endif
