#include <util/delay.h>
#include <util/atomic.h>
#include "settings.h"
#include "pin.h"
#include "1w.h"

//#define W1_IN()	getPin(PIN_1W)
//#define W1_HIGH() (setDDR(PIN_1W, 0), setPort(PIN_1W, 1))
//#define W1_LOW() (setDDR(PIN_1W, 1), setPort(PIN_1W, 0))

static char W1_IN(char n) {
	if (n)
		return getPin(PIN_1W_1);
	else
		return getPin(PIN_1W);
}

static void W1_HIGH(char n) {
	if (n) {
		setDDR(PIN_1W_1, 0);
		setPort(PIN_1W_1, 1);
	} else {
		setDDR(PIN_1W, 0);
		setPort(PIN_1W, 1);
	}
}

static void W1_LOW(char n) {
	if (n) {
		setDDR(PIN_1W_1, 1);
		setPort(PIN_1W_1, 0);
	} else {
		setDDR(PIN_1W, 1);
		setPort(PIN_1W, 0);
	}
}

#define CONVERT_TEMP        0x44 //Команда к началу измерения температуры.
#define WRITE_SCRATCHPAD    0x4E //Записать пользовательские данные в регистры TEMP_TL, TEMP_TH и находящиеся в ПЗУ. 
#define READ_SCRATCHPAD     0xBE //Прочесть содержимое ОЗУ (9 байт).
#define COPY_SCRATCHPAD     0x48 //Сохранение текущих значений регистров TEMP_TL\TH в ПЗУ.
#define RECALL_E2           0xB8 //Команда действует обратным образом предыдущей.
#define READ_POWER_SUPPLY   0xB4 //Проверка используемого режима питания (обычное \ паразитное питание).
#define SKIP_ROM		    0xCC //Пропустить процедуру сравнив. сер. номера.  //;
//***************************************************************************
unsigned char oneWireInit(char n) {
	char res;
	ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {
		W1_LOW(n);
		_delay_us(485);
		W1_HIGH(n);
		_delay_us(70);
		res = (W1_IN(n) == 0);
	}
	if (res == 0)
		return 0;
	_delay_us(440);
	return W1_IN(n) != 0;
}
//***************************************************************************
void oneWireWriteByte(char n, unsigned char data) {
	unsigned char i;
	unsigned char data_bit = data; //Переменная представляющая текущий бит для передачи.

	for (i = 0; i < 8; i++) {
		ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {
			W1_LOW(n);
			if (data_bit & 1) {
				_delay_us(1); //1мкс состояния линии "в ноль".
				W1_HIGH(n);
				_delay_us(90);
			} else {
				_delay_us(90); //Для передачи 0, задержим линию "в низком уровне" некоторое время.
				W1_HIGH(n);
				_delay_us(1);
			}
			_delay_us(1); //Выдерживаем минимальную задержку между тайм-слотами (1мкс.(продолжит. либого тайм-слота = 60-120мкс)).
		}
		data_bit >>= 1;
	}
}
//***************************************************************************

unsigned char oneWireReadByte(char n) {
	char i;
	unsigned char data = 0;

	for (i = 0; i < 8; i++) {
		ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {
			_delay_us(1);
			W1_LOW(n);
			//Как всегда перед передачей или приемом любого тайм-слота "опускаем линию",
			_delay_us(5); //давая ведомому устр-ву понять что мы готовы к приему данных.

			W1_HIGH(n);
			//Затем возвращаем в 1 и ждем что бы устр-во успело перевести линию в низкий уровень,
			_delay_us(15); //либо если идет передача единицы - оставило состояние шины как есть.

			data >>= 1;

			if (W1_IN(n)) //Если линия так и осталась при высоком уровне:
				data |= 0x80;

			_delay_us(55); //Заканчиваем текущий тайм-слот и переходим к началу следующего...
		}
	}
	return data; //Возвращаем принятый байт.
}

//***************************************************************************
static int oneWireStartConversion(char n) {
	if (!oneWireInit(n))
		return 0;
	oneWireWriteByte(n, SKIP_ROM);
	oneWireWriteByte(n, CONVERT_TEMP);
	return 1;
}

uint8_t doCRC8(uint8_t inData, uint8_t seed) {
	uint8_t bitsLeft;
	uint8_t temp;

	for (bitsLeft = 8; bitsLeft > 0; bitsLeft--) {
		temp = ((seed ^ inData) & 0x01);
		if (temp == 0) {
			seed >>= 1;
		} else {
			seed ^= 0x18;
			seed >>= 1;
			seed |= 0x80;
		}
		inData >>= 1;
	}
	return seed;
}

int checkCRC8(uint8_t* buf, int len) {
	uint8_t crc = 0;
	while (len-- > 1)
		crc = doCRC8(*(buf++), crc);
	return *buf == crc;
}

static int oneWireGetConversionResult(char n) {
	if (!oneWireInit(n))
		return TEMPERATURE_INVALID;
	oneWireWriteByte(n, SKIP_ROM);
	oneWireWriteByte(n, READ_SCRATCHPAD);

	unsigned char ds18b20[9];
	for (int i = 0; i < 9; i++)
		ds18b20[i] = oneWireReadByte(n);

	if(checkCRC8(ds18b20, 9))
		return ((ds18b20[1] << 8) + ds18b20[0]) >> 4;
	else
		return TEMPERATURE_INVALID;
}

int oneWireGetTemperature(char n) {
	if (!oneWireStartConversion(n))
		return TEMPERATURE_INVALID;
	_delay_ms(200);
	return oneWireGetConversionResult(n);
}

int temperature[2] = { TEMPERATURE_INVALID, TEMPERATURE_INVALID };

typedef struct {
	char state;
	int time;
	uint8_t buffer[9];
	int idx;
} DS1820STATE;

void ds18b20_step(int n, int ms) {
	static DS1820STATE states[2];
	DS1820STATE* state = &states[n];

	switch (state[n].state) {
	case 0:
		if (!oneWireInit(n)) {
			temperature[n] = TEMPERATURE_INVALID;
			state->state = 0;
			break;
		}
		state->state++;
		break;
	case 1:
		oneWireWriteByte(n, SKIP_ROM);
		state->state++;
		break;
	case 2:
		oneWireWriteByte(n, CONVERT_TEMP);
		state->time = 0;
		state->state++;
		break;
	case 3:
		state->time += ms;
		if (state->time < 200)
			break;
		state->state++;
		break;
	case 4:
		if (!oneWireInit(n)) {
			temperature[n] = TEMPERATURE_INVALID;
			state->state = 0;
			break;
		}
		state->state++;
		break;
	case 5:
		oneWireWriteByte(n, SKIP_ROM);
		state->state++;
		break;
	case 6:
		oneWireWriteByte(n, READ_SCRATCHPAD);
		state->state++;
		state->idx = 0;
		break;
	case 7:
		state->buffer[state->idx++] = oneWireReadByte(n);
		if(state->idx == 9) {
			if(checkCRC8(state->buffer, 9))
				temperature[n] = ((state->buffer[1] << 8) + state->buffer[0]) >> 4;
			else
				temperature[n] = TEMPERATURE_INVALID;
			state->state = 0;
		}
		break;
	}
}

