#include "heap.h"
#include "config.h"

#include <stdint.h>
#include <string.h>

#define REF_VALUE_START 0x7000

#define FLAG_MARKED 1
#define FLAG_ARRAY 2
#define FLAG_OBJARRAY 4

#define HEAP_SIZE 128

static char data[HEAP_SIZE];

static int here;
static int tempArr;
static int idxArr;
static int entries;

static int firstFree;

static int cnt;

typedef struct {
	uint8_t flags;
	uint8_t size;
	uint8_t entry;
	short data[0];
} Entry;

static void extendIdx();
static int _getEntry(int e);
static void _setEntry(int e, int16_t n);

void heap_init() {
	entries = 0;
	idxArr = HEAP_SIZE - entries * 2;
	tempArr = idxArr - entries;
	extendIdx();
	firstFree = 0;
}

static void _addToFree(int e) {
	_setEntry(e, firstFree);
	firstFree = e;
}

static int _getFree() {
	if (firstFree == 0)
		extendIdx();
	if (firstFree == 0)
		return 0;
	int e = firstFree;
	firstFree = _getEntry(firstFree);
	return e;
}

static void extendIdx() {
	if (tempArr - here >= 3) {
		entries++;
		idxArr = HEAP_SIZE - entries * 2;
		tempArr = idxArr - entries;
		_addToFree(entries - 1);
	}
}

//static int16_t _get(int off) {
//	return *(int16_t*) (&data[off]);
//}

//static void _set(int off, int16_t n) {
//	*(int16_t*) (&data[off]) = n;
//}

static int _getEntry(int e) {
	return ((int16_t*) (data + HEAP_SIZE))[-e - 1];
	//return _get(HEAP_SIZE - (e + 1) * 2);
}

static void _setEntry(int e, int16_t n) {
	((int16_t*) (data + HEAP_SIZE))[-e - 1] = n;
	//_set(HEAP_SIZE - (e + 1) * 2, n);
}

static Entry* offToEntry(int off) {
	return ((Entry*) &data[off]);
}

static int isValidRef(int a) {
	if (a == 0)
		return 0;

	a -= REF_VALUE_START;
	return a > 0 && a < entries && (_getEntry(a) & 0x8000) != 0;
}

int heapAlloc(int typeIdx_arrSize, int array, int objArray) {
	int e = _getFree();
	if (e == 0)
		return 0;

	int sz = array ? typeIdx_arrSize : getTypeSize(typeIdx_arrSize);

	int newHere = here + sizeof(Entry) + sz * 2;

	if (newHere > tempArr) {
		_addToFree(e);
		return 0;
	}

	int off = here;
	here = newHere;

	memset(&data[off], 0, here - off);

	int flags = 0;
	if (array)
		flags |= FLAG_ARRAY;
	if (objArray)
		flags |= FLAG_OBJARRAY;

	Entry* entry = offToEntry(off);
	entry->flags = flags;
	entry->entry = e;
	entry->size = typeIdx_arrSize;

	_setEntry(e, off | 0x8000);

	cnt++;

	vmPrintString("a ");
	return e + REF_VALUE_START;
}

int heapAlloc2(int typeIdx_arrSize, int array, int objArray) {
	int res = heapAlloc(typeIdx_arrSize, array, objArray);
	if (res == 0) {
		gc();
		return heapAlloc(typeIdx_arrSize, array, objArray);
	}
	return res;
}

int16_t heapGet(int a, int idx) {
	return offToEntry(_getEntry(a - REF_VALUE_START) & 0x7FFF)->data[idx];
}

void heapSet(int a, int idx, int16_t val) {
	offToEntry(_getEntry(a - REF_VALUE_START) & 0x7FFF)->data[idx] = val;
}

static int markIdx = 0;
static int markSz = 0;

void heapStartMark() {
	markIdx = 0;
	markSz = 0;
}

void heapMark(int a) {
	if (!isValidRef(a))
		return;

	int off = _getEntry(a - REF_VALUE_START) & 0x7FFF;
	Entry* entry = offToEntry(off);
	if ((entry->flags & FLAG_MARKED) != 0)
		return;
	entry->flags |= FLAG_MARKED;

	data[tempArr + markSz++] = a - REF_VALUE_START;
}

void heapMarkClosure() {
	while (markIdx < markSz) {
		int a = data[tempArr + markIdx];

		int off = _getEntry(a) & 0x7FFF;
		Entry* entry = offToEntry(off);

		if ((entry->flags & FLAG_ARRAY) == 0) {
			int typeIdx = entry->size;
			uint16_t mask = getTypeMask(typeIdx);
			int sz = getTypeSize(typeIdx);
			int i;
			for (i = 0; i < sz; i++) {
				if ((mask & 1) != 0)
					heapMark(entry->data[i]);
				mask >>= 1;
			}
		} else if ((entry->flags & FLAG_OBJARRAY) != 0) {
			int sz = entry->size;
			int i;
			for (i = 0; i < sz; i++)
				heapMark(entry->data[i]);
		}

		markIdx++;
	}
}

void heapSweep() {
	int dst = 0;
	int src = 0;

	int newCnt = 0;
	int i;
	for (i = 0; i < cnt; i++) {
		Entry* _src = offToEntry(src);
		Entry* _dst = offToEntry(dst);

		int sz;

		if ((_src->flags & FLAG_ARRAY) != 0)
			sz = sizeof(Entry) + _src->size * 2;
		else
			sz = sizeof(Entry) + getTypeSize(_src->size) * 2;

		int e = _src->entry;

		if ((_src->flags & FLAG_MARKED) == 0) {
			_addToFree(e);
			vmPrintString("f ");
		} else {
			_src->flags &= ~FLAG_MARKED;
			if (src != dst) {
				memcpy(_dst, _src, sz);
				_setEntry(e, dst | 0x8000);
			}
			dst += sz;
			newCnt++;
		}
		src += sz;
	}
	cnt = newCnt;
	here = dst;
}

int heapGetArraySize(int a) {
	Entry* entry = offToEntry(_getEntry(a - REF_VALUE_START) & 0x7FFF);
	return entry->size;
}

int heapGetTypeIdx(int a) {
	Entry* entry = offToEntry(_getEntry(a - REF_VALUE_START) & 0x7FFF);
	return entry->size;
}

