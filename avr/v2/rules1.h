#ifndef RULES1_H_
#define RULES1_H_

#define START_RULES_REG 256
#define RULES_LEN 256
#define RULES_ON_REG 512
#define RULES_ON_REG 512

int8_t rules1Step();

void setRules1(int16_t* data, int n);
int getRules1Size();
void setRules1Word(int a, int w);
int getRules1Word(int a);
int getRulesState();

#endif /* RULES1_H_ */
