/*
 * interpreter.h
 *
 *  Created on: 27.03.2013
 *      Author: kvv
 */

#ifndef INTERPRETER_H_
#define INTERPRETER_H_

#include <stdint.h>

int16_t vmGetReg(uint8_t reg);
void vmSetReg(uint8_t reg, int16_t val);

uint8_t vmReadByte(uint16_t addr);

void vmPrintInt(int16_t n);

void vmStart(int status);
void vmStep(int ms);
int vmCheckCode(); // returns codefile length

int vmGetStatus();
void vmSetStatus(int status);



#endif /* INTERPRETER_H_ */
