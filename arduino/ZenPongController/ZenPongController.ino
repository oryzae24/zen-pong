// Read controller inputs and drive the passive buzzer.
// VRx=A0, VRy=A1, SW=D2, slower=D4, faster=D5, visual=D6, buzzer=D8.
#include <Arduino.h>
#include <stdlib.h>
#include <string.h>

const uint8_t JOY_X=A0, JOY_Y=A1, PAUSE_PIN=2, SLOWER_PIN=4;
const uint8_t FASTER_PIN=5, VISUAL_PIN=6, BUZZER_PIN=8;
const unsigned long WATCHDOG_MS=1500;
int centerX=512,centerY=512;
bool hostSeen=false;
unsigned long lastHost=0, lastInput=0, lastReady=0;
char command[64]; uint8_t commandLength=0; bool overflow=false;

// Use internal pull-ups and accept a new state after 25 ms of stability.
struct Button {
  uint8_t pin; bool raw=false, stable=false; unsigned long changedAt=0;
  explicit Button(uint8_t p):pin(p) {}
  void begin() { pinMode(pin,INPUT_PULLUP); raw=stable=digitalRead(pin)==LOW; }
  void update(unsigned long now) {
    bool value=digitalRead(pin)==LOW;
    if(value!=raw) { raw=value; changedAt=now; }
    if(now-changedAt>=25) stable=raw;
  }
};
Button pauseButton(PAUSE_PIN), slowerButton(SLOWER_PIN), fasterButton(FASTER_PIN), visualButton(VISUAL_PIN);

void stopOutputs() { noTone(BUZZER_PIN); }
bool number(const char* text,long& result) {
  if(!text||!*text) return false;
  char* end; result=strtol(text,&end,10); return *end=='\0';
}
// H: heartbeat; S: screen state; T: frequency and duration.
void receive(char* line) {
  char* save=NULL;
  char* tag=strtok_r(line,",",&save);
  if(!tag) return;
  char* a=strtok_r(NULL,",",&save);
  char* b=strtok_r(NULL,",",&save);
  char* extra=strtok_r(NULL,",",&save);
  bool valid=false; long x=0,y=0;
  if(!strcmp(tag,"H")&&!a) valid=true;
  else if(!strcmp(tag,"S")&&a&&!b) {
    if(!strcmp(a,"PLAY")||!strcmp(a,"PAUSE")||!strcmp(a,"TITLE")) {
      stopOutputs(); valid=true;
    }
  } else if(!strcmp(tag,"T")&&!extra&&number(a,x)&&number(b,y)) {
    if(x>=0&&x<=5000&&y>=0&&y<=2000) {
      if(x==0||y==0) noTone(BUZZER_PIN);
      else tone(BUZZER_PIN,(unsigned int)x,(unsigned long)y);
      valid=true;
    }
  }
  if(valid) { lastHost=millis(); hostSeen=true; }
}
// Use a +/-65 dead zone and normalize each axis to -1000..1000.
int readAxis(uint8_t pin,int center) {
  int delta=analogRead(pin)-center;
  const int deadzone=65;
  if(abs(delta)<=deadzone) return 0;
  int extent=delta>0?1023-center:center;
  long value=(long)(abs(delta)-deadzone)*1000/max(1,extent-deadzone);
  return (delta>0?1:-1)*(int)constrain(value,0L,1000L);
}
void setup() {
  pinMode(BUZZER_PIN,OUTPUT); stopOutputs();
  pauseButton.begin(); slowerButton.begin(); fasterButton.begin();visualButton.begin();
  // Keep the stick centered; average 32 samples to calibrate both axes.
  long sumX=0,sumY=0;
  for(uint8_t i=0;i<32;i++) { sumX+=analogRead(JOY_X);sumY+=analogRead(JOY_Y);delay(2); }
  centerX=constrain(sumX/32,200L,823L);
  centerY=constrain(sumY/32,200L,823L);
  Serial.begin(115200); Serial.println(F("READY,ZENPONG,3"));
}
void loop() {
  unsigned long now=millis();
  pauseButton.update(now); slowerButton.update(now); fasterButton.update(now);visualButton.update(now);
  // Bound serial input work so button and watchdog updates keep running.
  for(uint8_t n=0;n<48 && Serial.available();n++) {
    char c=(char)Serial.read();
    if(c=='\r') continue;
    if(c=='\n') {
      if(!overflow) { command[commandLength]='\0'; receive(command); }
      commandLength=0; overflow=false;
    } else if(commandLength<sizeof(command)-1 && !overflow) command[commandLength++]=c;
    else overflow=true;
  }
  // Refresh time after command handling before checking the watchdog.
  now=millis();
  if(hostSeen && now-lastHost>WATCHDOG_MS) {
    // Stop the buzzer after 1.5 seconds without a valid host command.
    hostSeen=false; stopOutputs();
  }
  if(now-lastReady>=1000) { lastReady=now; Serial.println(F("READY,ZENPONG,3")); }
  // Send both axes and four button states every 20 ms.
  if(now-lastInput>=20) {
    lastInput=now;
    Serial.print(F("I,")); Serial.print(readAxis(JOY_X,centerX)); Serial.print(',');
    Serial.print(readAxis(JOY_Y,centerY)); Serial.print(',');
    Serial.print(pauseButton.stable?1:0); Serial.print(','); Serial.print(slowerButton.stable?1:0);
    Serial.print(','); Serial.print(fasterButton.stable?1:0);
    Serial.print(','); Serial.println(visualButton.stable?1:0);
  }
}
