#include <util/delay.h>
#include <util/atomic.h>
#include "settings.h"
#include "pin.h"

#define ONE_WIRE_PORTIN()	getPin(PIN_1W)
#define ONE_WIRE_PORT_1()	setPort(W1, 1);
#define ONE_WIRE_PORT_0()	setPort(PIN_1W, 0);
#define ONE_WIRE_DDR_1()	setDDR(PIN_1W, 1);
#define ONE_WIRE_DDR_0()	setDDR(PIN_1W, 0);

#define CONVERT_TEMP        0x44 //������� � ������ ��������� �����������.
#define WRITE_SCRATCHPAD    0x4E //�������� ���������������� ������ � �������� TEMP_TL, TEMP_TH � ����������� � ���. 
#define READ_SCRATCHPAD     0xBE //�������� ���������� ��� (9 ����).
#define COPY_SCRATCHPAD     0x48 //���������� ������� �������� ��������� TEMP_TL\TH � ���.
#define RECALL_E2           0xB8 //������� ��������� �������� ������� ����������.
#define READ_POWER_SUPPLY   0xB4 //�������� ������������� ������ ������� (������� \ ���������� �������).
#define SKIP_ROM		    0xCC //���������� ��������� �������. ���. ������.  //;
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
	unsigned char data_bit; //���������� �������������� ������� ��� ��� ��������.

	for (i = 0; i < 8; i++) //����  �������� 8 ���.
			{
		ATOMIC_BLOCK(ATOMIC_RESTORESTATE) {

			data_bit = data; //���������� �������, ������������ ��� ����������� ������ � ����������� "�".
			data_bit = (data >> i) & 1; //��� ������� ������������� ���� �������� ���������:
										//��� ��� ��� ������ ���������� ����� i=0, ������ ��� ���� ���������� data (��� ������ ������������ ���� ����)
										//��������� ������ �� 0 �������, �.� �� ���������� ������, ������ ��������� ���������� "�"
										//� ������������ ������, ��� ��� ������ ������� ����� �������, �� ��� ���������
										//7 ��� ���������� Data �������� ���������, ���������� �� ���� ������ ��� ���� ��
										//�����. ��������� ���� (i=1) ��������� ��� �� ����� �������� �� ������ ����� Data � �.�.

			ONE_WIRE_DDR_1(); //Low LINE
			ONE_WIRE_PORT_0(); //����� ����-���� ���������� ������� ���-��� � �������� ������� ������.
							   //����� � ����������� �� �������� ���� ��� ��������, ����������� ��������� ������������������,
							   //��������� ������ �� ���� ������ (�������):

			if (data_bit == 1) {
				_delay_us(1); //1��� ��������� ����� "� ����".
				ONE_WIRE_DDR_0(); //��������� ����� � ���. ��.
				_delay_us(90);
			} else {
				_delay_us(90); //��� �������� 0, �������� ����� "� ������ ������" ��������� �����.
				ONE_WIRE_DDR_0(); //��������� ����� � ���. ��.
				_delay_us(1);
			}
			ONE_WIRE_DDR_0(); //�� ���������� �������� ������� ���� ����� ���������� ���������� � ���. ��.
			_delay_us(1); //����������� ����������� �������� ����� ����-������� (1���.(���������. ������ ����-����� = 60-120���)).
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
			ONE_WIRE_PORT_0(); //��� ������ ����� ��������� ��� ������� ������ ����-����� "�������� �����",
			_delay_us(5); //����� �������� ����-�� ������ ��� �� ������ � ������ ������.

			ONE_WIRE_DDR_0(); //����� ���������� � 1 � ���� ��� �� ����-�� ������ ��������� ����� � ������ �������,
			_delay_us(15); //���� ���� ���� �������� ������� - �������� ��������� ���� ��� ����.

			if (ONE_WIRE_PORTIN() != 0) //���� ����� ��� � �������� ��� ������� ������:
				Data |= (1 << i); //�������� ���������� "���" � �������� � ������� �������� ���� ��� ������
			//(�.� ��-������� ������ ��������� 1). ����� ������� �� ������� ������� ��� ��� ������� ����-�� ����-
			//������ 1 � ���� �� ���� ���������� �������. ��� ���������� � ����� �� ����� ���
			//���������� ������� ����-��.
			_delay_us(55); //����������� ������� ����-���� � ��������� � ������ ����������...
		}
	}
	return Data; //���������� �������� ����.
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

