#include <avr/pgmspace.h>
#include <avr/boot.h>
#include "utils.h"
#include "packet.h"
#include "hw.h"

void spmWait() {
	boot_spm_busy_wait();
	eeprom_busy_wait();
}

void rww_enable() {
	spmWait();
	boot_rww_enable();
}

void page_write(uint16_t a) {
	spmWait();
	if (globals.magic16 == MAGIC16)
		boot_page_write(a);
	rww_enable();
}

#define LAST_PAGE_ADDR (BOOTLOADER_START - SPM_PAGESIZE)

uint8_t isAppOK() {
	spmWait();
	return pgm_read_dword(LAST_PAGE_ADDR) == MAGIC_APP_DWORD;
}

void setAppOK() {
	spmWait();
	boot_page_fill(LAST_PAGE_ADDR, MAGIC_APP_0);
	boot_page_fill(LAST_PAGE_ADDR + 2, MAGIC_APP_2);
	page_write(LAST_PAGE_ADDR);
}

void erase(uint16_t a) {
	spmWait();
	if (globals.magic16 == MAGIC16)
		boot_page_erase(a);
	rww_enable();
}

void processBlock(uint16_t a, uint16_t* data, uint8_t nwords) {
	do {
		erase(a);
		uint16_t a1 = a;
		do {
			spmWait();
			boot_page_fill(a, *data);
			a += 2;
			data++;
			nwords--;
		} while (a & (SPM_PAGESIZE - 1));
		page_write(a1);
	} while (nwords);
}

void packetReceived(uint8_t* buffer, int len) { // returns consumed flag
	ADU* adu = (ADU*) (buffer);

	if (len < 4)
		return;

	uint8_t addr = adu->addr;
	if (addr != MYADDR)
		return;

	uint16_t sum = crc16(buffer, len - 2);
	if ((buffer[len - 2] != (char) sum)
			|| (buffer[len - 1] != (char) (sum >> 8)))
		return;

	// ((0x3FFF + 1 - (512 * 2)) - 128)

	switch (adu->func) {
	case MODBUS_HELLO:
		break;
	case MODBUS_UPLOAD_APP: {
		uint16_t l = len - 6;
		l = (l + SPM_PAGESIZE - 1) & ~(SPM_PAGESIZE - 1);
		uint16_t a = BSWAP_16(*((uint16_t*)adu->data));

		if (a > LAST_PAGE_ADDR - l)
			goto err;

		processBlock(a, (uint16_t*) (adu->data + 2), l >> 1);
		break;
	}
	case MODBUS_ENABLE_APP:
		if (adu->data[0]) {
			//sendError(adu->func, 6);
			//return;
			setAppOK();
		} else {
			erase(LAST_PAGE_ADDR);
		}
		break;
	default:
		err: sendError(adu->func, 1);
		return;
	}
	sendOk(adu->func);
}

