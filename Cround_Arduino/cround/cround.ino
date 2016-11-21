#include <uspeech.h>
signal voice(A0);

boolean FLAG_1,FLAG_2,FLAG_3,FLAG_4,FLAG_H1,FLAG_H2;
boolean muteSend,siSend,noSend;

char SUBIR_CANAL_OP = '1';
char BAJAR_CANAL_OP = '2';
char SUBIR_VOLUMEN_OP = '3';
char BAJAR_VOLUMEN_OP = '4';
char MUTE_OP = '5';

int retardo = 200;  // milliseconds

void setup() {
  // put your setup code here, to run once:
  voice.calibrate();
  voice.f_enabled = true;
  voice.minVolume = 1000;
  voice.econstant = 1;
  voice.aconstant = 3;
  voice.vconstant = 5;
  voice.fconstant = 100;

  Serial.begin(9600);
  Serial3.begin(9600);

  FLAG_1 = false;
  FLAG_2 = false;
  FLAG_3 = false;
  FLAG_4 = false;
  FLAG_H1 = false;
  FLAG_H2 = false;
  muteSend = false;
  siSend = false;
  noSend = false;
}

int i = 0;

void loop() {
  // put your main code here, to run repeatedly:
  voice.sample();
  char p = voice.getPhoneme();
  if(p!=' '){
    //Serial.println(voice.testCoeff);
    //Serial.println(p);

  if(FLAG_H1 &&  p == 'h') {
      Serial.println("MUTE");
      Serial3.print(MUTE_OP);Serial3.println("+");
      delay(retardo);
      voice.testCoeff = 0;
      FLAG_H1 = false;
      muteSend = true;
      siSend = false;
      noSend = false;
    }    
    if(p == 'h') {
      FLAG_H1 = true;
      siSend = false;
      noSend = false;
    }


   
    
    if(FLAG_1 && voice.testCoeff == 1) {
      Serial.println("SI");
      Serial3.print(SUBIR_CANAL_OP);Serial3.println("+");
      delay(retardo);
      voice.testCoeff = 0;
      FLAG_1 = false;
      muteSend = false;
      siSend = true;
      noSend = false;
    }
    if(voice.testCoeff == 1) {
      FLAG_1 = true;
      muteSend = false;
      noSend = false;
    }
    

    if(FLAG_4 && voice.testCoeff == 4) {
      Serial.println("NO");
      Serial3.print(BAJAR_CANAL_OP);Serial3.println("+");
      delay(retardo);
      voice.testCoeff = 0;
      FLAG_4 = false;
      muteSend = false;
      siSend = false;
      noSend = true;
    }
    if(voice.testCoeff == 4) {
      FLAG_4 = true;
      muteSend = false;
      siSend = false;
    }

   
  }
  

}


