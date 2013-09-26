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

static int oneWireGetConversionResult(char n) {
	unsigned char ds18b20[2];
	if (!oneWireInit(n))
		return -9999;
	oneWireWriteByte(n, SKIP_ROM);
	oneWireWriteByte(n, READ_SCRATCHPAD);
	ds18b20[0] = oneWireReadByte(n);
	ds18b20[1] = oneWireReadByte(n);
	if (ds18b20[0] == 255 && ds18b20[1] == 255)
		return -9999;
	return ((ds18b20[1] << 8) + ds18b20[0]) >> 4;
}

int oneWireGetTemperature(char n) {
	if (!oneWireStartConversion(n))
		return -9999;
	_delay_ms(200);
	return oneWireGetConversionResult(n);
}

int temperature[2] = { TEMPERATURE_INVALID, TEMPERATURE_INVALID };

void ds18b20_step(int n, int ms) {
	static int state[2];
	static int time[2];
	static unsigned char ds18b20Hi[2];
	static unsigned char ds18b20Lo[2];

	switch (state[n]) {
	case 0:
		if (!oneWireInit(n)) {
			temperature[n] = TEMPERATURE_INVALID;
			state[n] = 0;
			break;
		}
		state[n]++;
		break;
	case 1:
		oneWireWriteByte(n, SKIP_ROM);
		state[n]++;
		break;
	case 2:
		oneWireWriteByte(n, CONVERT_TEMP);
		time[n] = 0;
		state[n]++;
		break;
	case 3:
		time[n] += ms;
		if (time[n] < 200)
			break;
		state[n]++;
		break;
	case 4:
		if (!oneWireInit(n)) {
			temperature[n] = TEMPERATURE_INVALID;
			state[n] = 0;
			break;
		}
		state[n]++;
		break;
	case 5:
		oneWireWriteByte(n, SKIP_ROM);
		state[n]++;
		break;
	case 6:
		oneWireWriteByte(n, READ_SCRATCHPAD);
		state[n]++;
		break;
	case 7:
		ds18b20Lo[n] = oneWireReadByte(n);
		state[n]++;
		break;
	case 8:
		ds18b20Hi[n] = oneWireReadByte(n);
		if (ds18b20Lo[n] == 255 && ds18b20Hi[n] == 255)
			temperature[n] = TEMPERATURE_INVALID;
		else
			temperature[n] = ((ds18b20Hi[n] << 8) + ds18b20Lo[n]) >> 4;
		state[n] = 0;
		break;
	}
}

