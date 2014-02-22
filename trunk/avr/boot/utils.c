#include <avr/pgmspace.h>
#include <avr/boot.h>
#include "utils.h"
#include "address.h"
#include "packet.h"
#include "hw.h"

uint8_t* getModbusData(uint8_t* buffer, uint8_t len, uint8_t* func, uint8_t* dataLen) {
	ADU* adu = (ADU*) (buffer);

	if (len < 3)
		return NULL;

	uint8_t addr = adu->addr;
	if (addr != MYADDR && addr != 0)
		return NULL;

	uint16_t sum = crc16(buffer, len - 2);

	if ((buffer[len - 2] != (char) sum)
			|| (buffer[len - 1] != (char) (sum >> 8))) {
		return NULL;
	}

	*func = adu->func;
	*dataLen = len - 4;
	return adu->data;
}

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

uint8_t isAppOK() {
	uint16_t lastPage = BOOTLOADER_START - SPM_PAGESIZE;
	return pgm_read_word(lastPage) == MAGIC_APP_0
			&& pgm_read_word(lastPage + 2) == MAGIC_APP_2;
}

void flushBuffer() {
	if (globals.lastPage != 0xFFFF) {
		boot_spm_busy_wait();
		eeprom_busy_wait();
		if (globals.magic16 == MAGIC16)
			boot_page_erase(globals.lastPage);
		boot_spm_busy_wait();
		if (globals.magic16 == MAGIC16)
			boot_page_write(globals.lastPage);
		globals.magic16 = 0;
		boot_spm_busy_wait();
		boot_rww_enable();
		globals.lastPage = 0xFFFF;
	}
}

void flash(uint16_t addr, uint16_t w) {
	if (globals.lastPage != (addr & ~(SPM_PAGESIZE - 1)))
		flushBuffer();
	boot_spm_busy_wait();
	eeprom_busy_wait();
	boot_page_fill(addr, w);
	globals.lastPage = addr & ~(SPM_PAGESIZE - 1);
}

void setAppOK() {
	if (isAppOK())
		return;
	uint16_t lastPage = BOOTLOADER_START - SPM_PAGESIZE;
	flash(lastPage, MAGIC_APP_0);
	flash(lastPage + 2, MAGIC_APP_2);
	flushBuffer();
}

void erase() {
	if (!isAppOK())
		return;
	boot_spm_busy_wait();
	eeprom_busy_wait();
	if (globals.magic16 == MAGIC16)
		boot_page_erase(BOOTLOADER_START - SPM_PAGESIZE);
	globals.magic16 = 0;
	boot_spm_busy_wait();
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
//		setAppOK();
		return 1;
	}

	return 0;
}

void packetReceived(uint8_t* buffer, int len) { // returns consumed flag
	uint8_t dataLen;
	uint8_t func;
	uint8_t* data = getModbusData(buffer, len, &func, &dataLen);
	if (!data)
		return;

	startTX();

	switch (func) {
	case MODBUS_HELLO:
		sendOk(func, wrByte);
		break;
	case MODBUS_CLEAR_APP:
		erase();
		globals.magic16 = 0;
		sendOk(func, wrByte);
		break;
	case MODBUS_UPLOAD_APP:
		if (processHexLine(data, dataLen))
			sendOk(func, wrByte);
		else
			sendError(func, 3, wrByte);
		globals.magic16 = 0;
		break;
	case MODBUS_APP_OK:
		setAppOK();
		globals.magic16 = 0;
		sendOk(func, wrByte);
		break;
	default:
		sendError(func, 1, wrByte);
		break;
	}

	stopTX();
}

