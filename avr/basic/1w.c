#include <util/delay.h>
#include <util/atomic.h>
#include "settings.h"
#include "pin.h"

#define W1_IN()	getPin(PIN_1W)
#define W1_HIGH() (setDDR(PIN_1W, 0), setPort(PIN_1W, 1))
#define W1_LOW() (setDDR(PIN_1W, 1), setPort(PIN_1W, 0))

#define CONVERT_TEMP        0x44 //������� � ������ ��������� �����������.
#define WRITE_SCRATCHPAD    0x4E //�������� ���������������� ������ � �������� TEMP_TL, TEMP_TH � ����������� � ���. 
#define READ_SCRATCHPAD     0xBE //�������� ���������� ��� (9 ����).
#define COPY_SCRATCHPAD     0x48 //���������� ������� �������� ��������� TEMP_TL\TH � ���.
#define RECALL_E2           0xB8 //������� ��������� �������� ������� ����������.
#define READ_POWER_SUPPLY   0xB4 //�������� ������������� ������ ������� (������� \ ���������� �������).
#define SKIP_ROM		    0xCC //���������� ��������� �������. ���. ������.  //;
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
	unsigned char data_bit = data; //���������� �������������� ������� ��� ��� ��������.

	for (i = 0; i < 8; i++) {
		ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {
			W1_LOW();
			if (data_bit & 1) {
				_delay_us(1); //1��� ��������� ����� "� ����".
				W1_HIGH();
				_delay_us(90);
			} else {
				_delay_us(90); //��� �������� 0, �������� ����� "� ������ ������" ��������� �����.
				W1_HIGH();
				_delay_us(1);
			}
			_delay_us(1); //����������� ����������� �������� ����� ����-������� (1���.(���������. ������ ����-����� = 60-120���)).
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
			//��� ������ ����� ��������� ��� ������� ������ ����-����� "�������� �����",
			_delay_us(5); //����� �������� ����-�� ������ ��� �� ������ � ������ ������.

			W1_HIGH();
			//����� ���������� � 1 � ���� ��� �� ����-�� ������ ��������� ����� � ������ �������,
			_delay_us(15); //���� ���� ���� �������� ������� - �������� ��������� ���� ��� ����.

			data >>= 1;

			if (W1_IN()) //���� ����� ��� � �������� ��� ������� ������:
				data |= 0x80;

			_delay_us(55); //����������� ������� ����-���� � ��������� � ������ ����������...
		}
	}
	return data; //���������� �������� ����.
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
