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

void vmStart(int8_t b);
void vmStep(int ms);
int vmCheckCode(); // returns codefile length

int8_t vmGetStatus();

void setState(int8_t s);
int16_t _getReg(uint8_t reg);
void _setReg(uint8_t reg, int16_t val);
int16_t vmPop();
void vmPush(int16_t v);
#define STACK_SIZE 16
extern int16_t stack[STACK_SIZE];
extern int16_t* stackPtr;


#endif /* INTERPRETER_H_ */
