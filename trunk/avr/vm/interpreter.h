/*
 * interpreter.h
 *
 *  Created on: 27.03.2013
 *      Author: kvv
 */

#ifndef INTERPRETER_H_
#define INTERPRETER_H_

#include <stdint.h>

int8_t vmGetReg(uint8_t reg, int16_t* val);
int8_t vmSetReg(uint8_t reg, int16_t val);

uint8_t vmReadByte(uint16_t addr);

void vmPrintInt(int16_t n);

int8_t vmGetStatus();

void vmSetStatus(int8_t s);
int16_t _getReg(uint8_t reg);
void _setReg(uint8_t reg, int16_t val);
int16_t vmPop();
void vmPush(int16_t v);
void vmSetStack(int16_t* ptr);
void vmChangeStack(int16_t n);

#define STACK_SIZE 32
extern int16_t stack[STACK_SIZE];
extern int16_t* stackPtr;

int vmGetRefsCnt();
int vmGetRefReg(int n);

int getUIStart();
int getUIEnd();

int16_t vmExec(uint16_t addr);
int16_t vmExec1(uint16_t addr, int16_t param);
int16_t vmExec2(uint16_t addr, int16_t param1, int16_t param2);
int16_t vmExec3(uint16_t addr, int16_t param1, int16_t param2, int16_t param3);
int16_t eval(uint16_t ip);
int16_t eval1(uint16_t ip, int16_t param);
void initVars();

uint16_t vmGetFuncCode(uint8_t func);
uint16_t getVMethod(uint8_t typeIdx, uint8_t methodIdx);
#define getVMethod1(obj, methodIdx)	getVMethod(heapGetTypeIdx(obj), (methodIdx))

#endif /* INTERPRETER_H_ */
