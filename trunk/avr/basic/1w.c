#include <util/delay.h>
#include <util/atomic.h>
#include "settings.h"
#include "pin.h"

#define ONE_WIRE_PORTIN()	getPin(PIN_1W)
#define ONE_WIRE_PORT_1()	setPort(W1, 1);
#define ONE_WIRE_PORT_0()	setPort(PIN_1W, 0);
#define ONE_WIRE_DDR_1()	setDDR(PIN_1W, 1);
#define ONE_WIRE_DDR_0()	setDDR(PIN_1W, 0);

#define CONVERT_TEMP        0x44 //Команда к началу измерения температуры.
#define WRITE_SCRATCHPAD    0x4E //Записать пользовательские данные в регистры TEMP_TL, TEMP_TH и находящиеся в ПЗУ. 
#define READ_SCRATCHPAD     0xBE //Прочесть содержимое ОЗУ (9 байт).
#define COPY_SCRATCHPAD     0x48 //Сохранение текущих значений регистров TEMP_TL\TH в ПЗУ.
#define RECALL_E2           0xB8 //Команда действует обратным образом предыдущей.
#define READ_POWER_SUPPLY   0xB4 //Проверка используемого режима питания (обычное \ паразитное питание).
#define SKIP_ROM		    0xCC //Пропустить процедуру сравнив. сер. номера.  //;
static unsigned char ds18b20[2];

#define DELAY(us) _delay_ms(us)

//getPortBit((((int)(&(*(volatile uint8_t *)((0x10) + 0x20))) << 8) | (1<<5)), 0)
//((*(volatile uint8_t *)((0x10) + 0x20)) & (1 << 5))
//***************************************************************************
unsigned char oneWireInit() {
	char res;
	ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {
		ONE_WIRE_DDR_1(); //Low LINE
		ONE_WIRE_PORT_0();
		_delay_us(485);
		ONE_WIRE_DDR_0(); //Hi LINE
		_delay_us(70);
		res = (ONE_WIRE_PORTIN() == 0);
	}
	if (res == 0)
		return 0;
	_delay_us(440);
	return ONE_WIRE_PORTIN() != 0;
}
//***************************************************************************
void oneWireWriteByte(unsigned char data) {
	unsigned char i;
	unsigned char data_bit; //Переменная представляющая текущий бит для передачи.

	for (i = 0; i < 8; i++) //Цикл  передачи 8 бит.
			{
		ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {

			data_bit = data; //Определяем текущий, передаваемый бит посредством сдвига и логического "И".
			data_bit = (data >> i) & 1; //Для первого передаваемого бита алгоритм следующий:
										//Так как при первом выполнении цикла i=0, значит все биты переменной data (она хранит передаваемый нами байт)
										//сдигаются вправо на 0 позиций, т.е не сдвигаются вообще, теперь выполняем логическое "И"
										//с получившимся числом, так как второй операнд равен единице, то все остальные
										//7 бит переменной Data попросту обнулятся, независимо от того какими они были до
										//этого. Следующий цикл (i=1) проделает эту же самую операцию со вторым битом Data и т.д.

			ONE_WIRE_DDR_1(); //Low LINE
			ONE_WIRE_PORT_0(); //Любой тайм-слот начинается ведущим уст-вом с передачи низкого уровня.
							   //Далее в зависимости от текущего бита для передачи, формируются временные последовательности,
							   //состоящие всегда из двух частей (условно):

			if (data_bit == 1) {
				_delay_us(1); //1мкс состояния линии "в ноль".
				ONE_WIRE_DDR_0(); //Возващаем линию в выс. ур.
				_delay_us(90);
			} else {
				_delay_us(90); //Для передачи 0, задержим линию "в низком уровне" некоторое время.
				ONE_WIRE_DDR_0(); //Возващаем линию в выс. ур.
				_delay_us(1);
			}
			ONE_WIRE_DDR_0(); //По завершении передачи каждого бита линию необходимо переводить в выс. ур.
			_delay_us(1); //Выдерживаем минимальную задержку между тайм-слотами (1мкс.(продолжит. либого тайм-слота = 60-120мкс)).
		}
	}
}
//***************************************************************************

unsigned char oneWireReadByte(void) {
	char i;
	unsigned char Data = 0;

	for (i = 0; i < 8; i++) {
		ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {
			_delay_us(1);
			ONE_WIRE_DDR_1(); //Low LINE
			ONE_WIRE_PORT_0(); //Как всегда перед передачей или приемом либого тайм-слота "опускаем линию",
			_delay_us(5); //давая ведомому устр-ву понять что мы готовы к приему данных.

			ONE_WIRE_DDR_0(); //Затем возвращаем в 1 и ждем что бы устр-во успело перевести линию в низкий уровень,
			_delay_us(15); //либо если идет передача единицы - оставило состояние шины как есть.

			if (ONE_WIRE_PORTIN() != 0) //Если линия так и осталась при высоком уровне:
				Data |= (1 << i); //Проведем логическое "ИЛИ" с единицей и номером текущего бита для чтения
			//(т.е по-другому говоря установим 1). Таким образом мы получим единицы там где ведомое устр-во пере-
			//давало 1 и нули во всех оставшихся случаях. Это собственно и будет то число что
			//передавало ведомое устр-во.
			_delay_us(55); //Заканчиваем текущий тайм-слот и переходим к началу следующего...
		}
	}
	return Data; //Возвращаем принятый байт.
}

//***************************************************************************
//void startConvert() {
//	oneWireInit();
//	oneWireWriteByte(SKIP_ROM);
//	oneWireWriteByte(CONVERT_TEMP);
//}
//***************************************************************************
int getTemperature() {
	if (!oneWireInit())
		return -9999;
	oneWireWriteByte(SKIP_ROM);
	oneWireWriteByte(CONVERT_TEMP);
	_delay_ms(100);
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

