#include <util/delay.h>
#include <util/atomic.h>
#include "settings.h"
#include "pin.h"

#define W1_IN()	getPin(PIN_1W)
#define W1_HIGH() (setDDR(PIN_1W, 0), setPort(PIN_1W, 1))
#define W1_LOW() (setDDR(PIN_1W, 1), setPort(PIN_1W, 0))

#define CONVERT_TEMP        0x44 //Команда к началу измерения температуры.
#define WRITE_SCRATCHPAD    0x4E //Записать пользовательские данные в регистры TEMP_TL, TEMP_TH и находящиеся в ПЗУ. 
#define READ_SCRATCHPAD     0xBE //Прочесть содержимое ОЗУ (9 байт).
#define COPY_SCRATCHPAD     0x48 //Сохранение текущих значений регистров TEMP_TL\TH в ПЗУ.
#define RECALL_E2           0xB8 //Команда действует обратным образом предыдущей.
#define READ_POWER_SUPPLY   0xB4 //Проверка используемого режима питания (обычное \ паразитное питание).
#define SKIP_ROM		    0xCC //Пропустить процедуру сравнив. сер. номера.  //;
//***************************************************************************
unsigned char oneWireInit() {
	char res;
	ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {
		W1_LOW();
		_delay_us(485);
		W1_HIGH();
		_delay_us(70);
		res = (W1_IN() == 0);
	}
	if (res == 0)
		return 0;
	_delay_us(440);
	return W1_IN() != 0;
}
//***************************************************************************
void oneWireWriteByte(unsigned char data) {
	unsigned char i;
	unsigned char data_bit = data; //Переменная представляющая текущий бит для передачи.

	for (i = 0; i < 8; i++) {
		ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {
			W1_LOW();
			if (data_bit & 1) {
				_delay_us(1); //1мкс состояния линии "в ноль".
				W1_HIGH();
				_delay_us(90);
			} else {
				_delay_us(90); //Для передачи 0, задержим линию "в низком уровне" некоторое время.
				W1_HIGH();
				_delay_us(1);
			}
			_delay_us(1); //Выдерживаем минимальную задержку между тайм-слотами (1мкс.(продолжит. либого тайм-слота = 60-120мкс)).
		}
		data_bit >>= 1;
	}
}
//***************************************************************************

unsigned char oneWireReadByte(void) {
	char i;
	unsigned char data = 0;

	for (i = 0; i < 8; i++) {
		ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {
			_delay_us(1);
			W1_LOW();
			//Как всегда перед передачей или приемом любого тайм-слота "опускаем линию",
			_delay_us(5); //давая ведомому устр-ву понять что мы готовы к приему данных.

			W1_HIGH();
			//Затем возвращаем в 1 и ждем что бы устр-во успело перевести линию в низкий уровень,
			_delay_us(15); //либо если идет передача единицы - оставило состояние шины как есть.

			data >>= 1;

			if (W1_IN()) //Если линия так и осталась при высоком уровне:
				data |= 0x80;

			_delay_us(55); //Заканчиваем текущий тайм-слот и переходим к началу следующего...
		}
	}
	return data; //Возвращаем принятый байт.
}

//***************************************************************************
int startConversion() {
	if (!oneWireInit())
		return 0;
	oneWireWriteByte(SKIP_ROM);
	oneWireWriteByte(CONVERT_TEMP);
	return 1;
}

int getConversionResult() {
	unsigned char ds18b20[2];
	if (!oneWireInit())
		return -9999;
	oneWireWriteByte(SKIP_ROM);
	oneWireWriteByte(READ_SCRATCHPAD);
	ds18b20[0] = oneWireReadByte();
	ds18b20[1] = oneWireReadByte();
	if (ds18b20[0] == 255 && ds18b20[1] == 255)
		return -9999;
	return ((ds18b20[1] << 8) + ds18b20[0]) >> 4;
}

int getTemperature() {
	if (!startConversion())
		return -9999;
	_delay_ms(100);
	return getConversionResult();
}

int temperature = -9999;

void ds18b20_step(int ms) {
	static int state;
	static int time;
	static unsigned char ds18b20[2];

	switch (state) {
	case 0:
		if (!oneWireInit()) {
			temperature = -9999;
			state = 0;
			break;
		}
		state++;
		break;
	case 1:
		oneWireWriteByte(SKIP_ROM);
		state++;
		break;
	case 2:
		oneWireWriteByte(CONVERT_TEMP);
		time = 0;
		state++;
		break;
	case 3:
		time += ms;
		if (time < 200)
			break;
		state++;
		break;
	case 4:
		if (!oneWireInit()) {
			temperature = -9999;
			state = 0;
			break;
		}
		state++;
		break;
	case 5:
		oneWireWriteByte(SKIP_ROM);
		state++;
		break;
	case 6:
		oneWireWriteByte(READ_SCRATCHPAD);
		state++;
		break;
	case 7:
		ds18b20[0] = oneWireReadByte();
		state++;
		break;
	case 8:
		ds18b20[1] = oneWireReadByte();
		if (ds18b20[0] == 255 && ds18b20[1] == 255)
			temperature = -9999;
		else
			temperature = ((ds18b20[1] << 8) + ds18b20[0]) >> 4;
		state = 0;
		break;
	}
}
