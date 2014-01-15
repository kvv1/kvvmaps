/*
 * vmstatus.h
 *
 *  Created on: 06.04.2013
 *      Author: kvv
 */

#ifndef VMSTATUS_H_
#define VMSTATUS_H_

#define VMSTATUS_STOPPED 0
#define VMSTATUS_RUNNING 1
#define VMSTATUS_PAUSED 2
#define VMSTATUS_WRONG_CHECKSUM 3
#define VMSTATUS_STACK_OVERFLOW 4
#define VMSTATUS_STACK_UNDERFLOW 5
#define VMSTATUS_INVALID_REGISTER 6
#define VMSTATUS_INVALID_BYTECODE 7

#define ARITHMETIC_EXCEPTION 1

#endif /* VMSTATUS_H_ */
