/*
 * vm.h
 *
 *  Created on: 05.04.2013
 *      Author: kvv
 */

#ifndef VM_H_
#define VM_H_

#include <avr/eeprom.h>
#include <stdint.h>

#define VMCODE_SIZE 480
extern EEMEM uint8_t code[VMCODE_SIZE];

void initVM();
void startVM(int8_t start);

int8_t vmGetStatus();
void vmStep(int ms);

int vmCheckCode(); // returns codefile length
uint8_t vmReadByte(uint16_t addr);

ee_8_decl(vmonoff);

#endif /* VM_H_ */
