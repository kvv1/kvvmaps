#include "array.h"
#include "heap.h"

void array_init(Array* arr, int objArr) {
	arr->objArr = objArr;
	arr->a = heapAlloc2(2, 1, objArr);
	arr->sz = 0;
}

int array_size(Array* arr) {
	return arr->sz;
}

uint16_t array_getAt(Array* arr, int idx) {
	return heapGet(arr->a, idx);
}

void array_setAt(Array* arr, int idx, uint16_t val) {
	heapSet(arr->a, idx, val);
}

int array_add(Array* arr, uint16_t val) {
	if (arr->sz == heapGetArraySize(arr->a)) {
		int aa = heapAlloc2(arr->sz + 2, 1, arr->objArr);
		int i;
		for (i = 0; i < arr->sz; i++)
			heapSet(aa, i, heapGet(arr->a, i));
		arr->a = aa;
	}
	heapSet(arr->a, arr->sz++, val);
	return arr->sz - 1;
}

void array_compact(Array* arr) {
	int i = 0;
	int k;
	for (k = 0; k < arr->sz; k++) {
		int val = array_getAt(arr, k);
		if (val != 0) {
			if (i != k) {
				array_setAt(arr, i, val);
				array_setAt(arr, k, 0);
			}
			i++;
		}
	}
	arr->sz = i;
}

void array_clear(Array* arr, uint16_t val) {
	int i;
	for (i = 0; i < arr->sz; i++)
		if (array_getAt(arr, i) == val)
			array_setAt(arr, i, 0);
}
