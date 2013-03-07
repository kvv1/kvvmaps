/*
 * myio.h
 *
 *  Created on: 04.03.2013
 *      Author: kvv
 */

#ifndef MYIO_H_
#define MYIO_H_

#include "settings.h"

void uart_init();

void uart_putchar(char c);

void print2( char* format, int n1, int n2);
void print1( char* format, int n1);
void print0( char* format);

#ifdef TRACE
#define trace0(x) print0(x)
#define trace1(x,y) print1(x,y)
#define trace2(x,y,z) print2(x,y,z)
#else
#define trace0(x)
#define trace1(x,y)
#define trace2(x,y,z)
#endif


#endif /* MYIO_H_ */
