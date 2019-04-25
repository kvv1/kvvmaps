#include <avr/io.h>
#include <avr/interrupt.h>

#define RS485_DDR DDRB
#define RS485_PORT PORTB
#define RS485_BIT 7

#define BUF_SZ 512

static volatile uint8_t buffer[BUF_SZ];
static int head;
static int tail;
static volatile int cnt;

static int packetStart;
static int packetCnt;

static uint8_t bufferGet() {
	cli();
	if (cnt == 0) {
		sei();
		return 0;
	}
	cnt--;
	sei();

	uint8_t res = buffer[tail++];
	if (tail == BUF_SZ)
		tail = 0;
	return res;
}

void rs485put(char b) {
	if (packetStart < 0) {
		packetStart = head;
		packetCnt = 0;
		rs485put(0);
	}

	cli();
	if (cnt >= BUF_SZ) {
		sei();
		return;
	}
	cnt++;
	sei();

	buffer[head++] = b;
	if (head == BUF_SZ)
		head = 0;
	packetCnt++;
}

void rs485endOfPacket() {
	if (packetStart < 0)
		return;

	buffer[packetStart] = packetCnt;
	packetStart = -1;
	packetCnt = 0;
}

int8_t rs485sendBufferIfValid(uint16_t* id) {
	if (cnt == 0)
		return 0;

	int c = buffer[tail];

	if (c == 0)
		return 0;

	if (c < 5)
		goto err;

	bufferGet();
	uint8_t id0 = bufferGet();
	uint8_t id1 = bufferGet();
	uint8_t id2 = bufferGet();
	uint8_t id3 = bufferGet();

	if ((id0 ^ id2) != 255 || (id1 ^ id3) != 255)
		goto err;

	*id = ((int) id0 << 8) | id1;
	c -= 5;

	RS485_PORT |= (1 << RS485_BIT);
	while (c--) {
		while (!(UCSRA & (1 << UDRE)))
			;
		UDR = bufferGet();
		UCSRA |= (1 << TXC);
	}
	while (!(UCSRA & (1 << TXC)))
		;
	RS485_PORT &= ~(1 << RS485_BIT);

	return 1;

	err: while (c--)
		bufferGet();
	return 0;
}
