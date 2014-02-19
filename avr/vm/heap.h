#include <stdint.h>

uint8_t getTypeSize(uint8_t typeIdx);
uint16_t getTypeMask(uint8_t typeIdx);

void heap_init();
//int heapAlloc(uint8_t typeIdx_arrSize, uint8_t array, uint8_t objArray);
int heapAlloc2(uint8_t typeIdx_arrSize, uint8_t array, uint8_t objArray);
void heapSet(int a, uint8_t idx, int16_t val);
int16_t heapGet(int a, uint8_t idx);
uint8_t heapGetArraySize(int a);
uint8_t heapGetTypeIdx(int a);
void heapStartMark();
void heapMark(int a);
void heapMarkClosure();
void heapSweep();

void gc();
