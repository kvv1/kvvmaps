#include <avr/pgmspace.h>
#include <avr/boot.h>
#include "utils.h"
#include "address.h"
#include "packet.h"
#include "hw.h"

static uint8_t ascii2digit(uint8_t c) {
	if (c >= 0x41)
		return c - 0x41 + 10;
	return c - 0x30;
}

static uint8_t fetchByteHex(uint8_t* ptr) {
	return (ascii2digit(ptr[0]) << 4) + ascii2digit(ptr[1]);
}

static int fetchIntHex(uint8_t* ptr) {
	return (fetchByteHex(ptr) << 8) + fetchByteHex(ptr + 2);
}

static void spmWait() {
	boot_spm_busy_wait();
	eeprom_busy_wait();
}

#define LAST_PAGE_ADDR (BOOTLOADER_START - SPM_PAGESIZE)


uint8_t isAppOK() {
	spmWait();
	uint16_t lastPage = LAST_PAGE_ADDR;
	return pgm_read_word(lastPage) == MAGIC_APP_0
			&& pgm_read_word(lastPage + 2) == MAGIC_APP_2;
}

void flushBuffer() {
	if (globals.lastPage != 0xFFFF) {
		spmWait();
		if (globals.magic16 == MAGIC16)
			boot_page_erase(globals.lastPage);
		spmWait();
		if (globals.magic16 == MAGIC16)
			boot_page_write(globals.lastPage);
		spmWait();
		boot_rww_enable();
		globals.lastPage = 0xFFFF;
	}
}

void flash(uint16_t addr, uint16_t w) {
	if (globals.lastPage != (addr & ~(SPM_PAGESIZE - 1)))
		flushBuffer();
	spmWait();
	boot_page_fill(addr, w);
	globals.lastPage = addr & ~(SPM_PAGESIZE - 1);
}

void setAppOK() {
	if (isAppOK())
		return;
	flash(LAST_PAGE_ADDR, MAGIC_APP_0);
	flash(LAST_PAGE_ADDR + 2, MAGIC_APP_2);
	flushBuffer();
}

void erase() {
	if (!isAppOK())
		return;
	spmWait();
	if (globals.magic16 == MAGIC16)
		boot_page_erase(LAST_PAGE_ADDR);
	spmWait();
	boot_rww_enable();
}

uint8_t processHexLine(uint8_t* line, uint8_t len) {

	if (isAppOK())
		return 0;

	if (line[0] != ':')
		return 0;

	uint8_t sum = 0;
	int i;
	for (i = 1; i < len; i += 2)
		sum += fetchByteHex(line + i);

	if (sum != 0)
		return 0;

	uint8_t recordType = fetchByteHex(line + 7);

	if (recordType == 0) {
		//return 1;
		uint8_t byteCnt = fetchByteHex(line + 1);
		int addr = fetchIntHex(line + 3);
		uint8_t i;
		for (i = 0; i < byteCnt; i += 2, addr += 2)
			flash(addr,
					fetchByteHex(line + 9 + i * 2)
							| (fetchByteHex(line + 9 + i * 2 + 2) << 8));
		return 1;
	} else if (recordType == 1) {
		flushBuffer();
		setAppOK();
		spmWait();

//		startTX();
//		wrByte(33);
//		wrByte(isAppOK());
//		wrByte(startCnt >> 8);
//		wrByte(startCnt);
//		stopTX();

		return 1;
	}

	return 0;
}

uint8_t checkPacket(uint8_t* buffer, uint16_t len) {
	ADU* adu = (ADU*) (buffer);

	if (len < 3)
		return 0;

	uint8_t addr = adu->addr;
	if (addr != MYADDR && addr != 0)
		return 0;

	uint16_t sum = crc16(buffer, len - 2);

	if ((buffer[len - 2] != (char) sum)
			|| (buffer[len - 1] != (char) (sum >> 8))) {
		return 0;
	}

	return 1;
}

void packetReceived(uint8_t* buffer, uint16_t len) { // returns consumed flag
	globals.magic16 = MAGIC16;

	if(!checkPacket(buffer, len))
		return;

	ADU* adu = (ADU*) (buffer);

	uint8_t func = adu->func;

	switch (func) {
	case MODBUS_HELLO:
		break;
	case MODBUS_CLEAR_APP:
		erase();
		break;
	case MODBUS_UPLOAD_APP:
		if (processHexLine(adu->data, len - 4)) {
			break;
		}
		globals.magic16 = 0;
		sendError(func, 3);
		return;
	case MODBUS_ENABLE_APP:
		setAppOK();
		spmWait();
		break;
	default:
		sendError(func, 1);
		return;
	}
	globals.magic16 = 0;
	sendOk(func);
}

