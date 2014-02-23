#include <avr/pgmspace.h>
#include <avr/boot.h>
#include "utils.h"
#include "packet.h"
#include "hw.h"

static void spmWait() {
	boot_spm_busy_wait();
	eeprom_busy_wait();
}

static void rww_enable() {
	spmWait();
	boot_rww_enable();
}

static void page_write(uint16_t a) {
	spmWait();
	if (globals.magic16 == MAGIC16)
		boot_page_write(a);
	rww_enable();
}

#define LAST_PAGE_ADDR (BOOTLOADER_START - SPM_PAGESIZE)

uint8_t isAppOK() {
	spmWait();
	return pgm_read_word(LAST_PAGE_ADDR) == MAGIC_APP_0
			&& pgm_read_word(LAST_PAGE_ADDR + 2) == MAGIC_APP_2;
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

#define BSWAP_16(x) ( (uint8_t)((x) >> 8) | ((uint8_t)(x)) << 8 )

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

	if (len < 3)
		return;

	uint8_t addr = adu->addr;
	if (addr != MYADDR && addr != 0)
		return;

	uint16_t sum = crc16(buffer, len - 2);

	if ((buffer[len - 2] != (char) sum)
			|| (buffer[len - 1] != (char) (sum >> 8)))
		return;

	switch (adu->func) {
	case MODBUS_HELLO:
		break;
	case MODBUS_CLEAR_APP:
		erase(LAST_PAGE_ADDR);
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
		setAppOK();
		spmWait();
		break;
	default:
		err:
		sendError(adu->func, 1);
		return;
	}
	sendOk(adu->func);
}

