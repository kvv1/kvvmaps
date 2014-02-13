#include <stdint.h>

int getTypeSize(int typeIdx);
uint16_t getTypeMask(int typeIdx);

void heap_init();
int heapAlloc(int typeIdx_arrSize, int array, int objArray);
int heapAlloc2(int typeIdx_arrSize, int array, int objArray);
void heapSet(int a, int idx, int16_t val);
int16_t heapGet(int a, int idx);
int heapGetArraySize(int a);
int heapGetTypeIdx(int a);
void heapStartMark();
void heapMark(int a);
void heapMarkClosure();
void heapSweep();

void gc();
