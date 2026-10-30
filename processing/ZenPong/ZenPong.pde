import processing.serial.*;
import javax.sound.sampled.*;

// Manage game state and input.
final int BAUD=115200;
final String ARDUINO_PORT=""; // Empty means keyboard-only startup; set a serial port to connect automatically.
final boolean INVERT_JOYSTICK_X=false;
final boolean INVERT_JOYSTICK_Y=false; // Reverse the menu axis if the joystick is mounted the other way.
PongPhysics game=new PongPhysics();
HardwareLink hardware;
GameAudio audio;
VisualEffects effects=new VisualEffects();
boolean started=false,paused=false,pcSound=true;
int pauseChoice=0;
boolean menuStickReady=false;
volatile float pcVolume=.5;
boolean leftHeld,rightHeld;
java.util.HashSet<Integer> heldKeys=new java.util.HashSet<Integer>();
long previousNanos;
double accumulator=0;
final float STEP=1.0/240; // Update physics at a fixed 240 Hz.

void settings() { size(820,600);pixelDensity(1);smooth(4); }
void setup() {
  surface.setTitle("ZEN PONG");textFont(createFont("SansSerif",16,true));
  hardware=new HardwareLink();audio=new GameAudio();previousNanos=System.nanoTime();frameRate(60);
  if(!ARDUINO_PORT.isEmpty())hardware.openPort(ARDUINO_PORT);
}
void draw() {
  long now=System.nanoTime();float dt=min(.05,(now-previousNanos)/1000000000.0);previousNanos=now;
  hardware.update();
  if(!focused) { clearKeys();if(started&&!paused) setPaused(true); }
  float input=(rightHeld?1:0)-(leftHeld?1:0);
  // Use fresh controller input only when no movement key is held.
  if(input==0&&hardware.fresh()) input=hardware.axisX;
  if(started&&!paused) {
    accumulator+=dt;while(accumulator>=STEP) { stepGame(STEP,input);accumulator-=STEP; }
  } else accumulator=0;
  audio.update();renderGame();
}
// Send collision events to audio and visual effects.
void stepGame(float dt,float input){
  effects.tick(dt);game.update(dt,input);
  while(!game.events.isEmpty()){
    PongPhysics.Event e=game.events.remove();audio.effect(e.kind);
    if(e.kind.equals("MISS"))effects.cut();else effects.impact(e.x,e.y,game.ballX,game.ballY);
  }
  if(game.respawn<=0)effects.sample(dt,game.ballX,game.ballY);
}
void togglePlay() {
  if(!started) { started=true;paused=false;hardware.state("PLAY");audio.effect("START"); }
  else if(paused&&pauseChoice==1)returnToTitle();
  else setPaused(!paused);
  accumulator=0;
}
void setPaused(boolean value) {
  if(value){pauseChoice=0;menuStickReady=false;clearKeys();}
  paused=value;accumulator=0;hardware.state(paused?"PAUSE":"PLAY");audio.stop();
}
// Require a neutral stick before accepting another menu selection.
void selectPause(float axis) {
  if(!started||!paused)return;
  if(abs(axis)<.25)menuStickReady=true;
  else if(menuStickReady&&abs(axis)>.55){pauseChoice=axis>0?1:0;menuStickReady=false;}
}
// Keep speed and visual mode when returning to the title.
void returnToTitle() {
  game.reset();effects.clear();started=paused=false;accumulator=0;clearKeys();audio.stop();hardware.state("TITLE");
}
void mousePressed() {
  if(!started||!paused||mouseButton!=LEFT||mouseX<170||mouseX>390)return;
  for(int i=0;i<2;i++)if(mouseY>=overlayCenterY()-14+i*46&&mouseY<=overlayCenterY()+22+i*46){pauseChoice=i;togglePlay();return;}
}
void clearKeys() { leftHeld=rightHeld=false;heldKeys.clear(); }
int keyId() { return key==CODED?65536+keyCode:Character.toLowerCase(key); }
void keyPressed() {
  if(key==ESC) { key=0;if(started&&!paused)setPaused(true);return; }
  movementKey(true);
  if(key=='[')pcVolume=max(0,pcVolume-.05);
  if(key==']')pcVolume=min(1,pcVolume+.05);
  int code=keyId();
  if(!heldKeys.add(code))return; // Ignore repeated key-down events.
  switch(Character.toLowerCase(key)) {
    case '\n':case '\r':case 'p':togglePlay();break;
    case '-':game.changeSpeed(-1);break;
    case '+':case '=':game.changeSpeed(1);break;
    case 'c':hardware.cyclePort();break;
    case 'x':hardware.disconnect();break;
    case 'r':returnToTitle();break;
    case 'v':effects.next();break;
    case 'm':pcSound=!pcSound;if(!pcSound)audio.pc.silence();break;
  }
  heldKeys.add(code); // Keep the confirmation key latched across state transitions.
}
void keyReleased() { movementKey(false);heldKeys.remove(keyId()); }
void movementKey(boolean held) {
  // Route vertical input to the pause menu.
  if(started&&paused&&held){
    if(key=='w'||key=='W'||(key==CODED&&keyCode==UP))pauseChoice=0;
    if(key=='s'||key=='S'||(key==CODED&&keyCode==DOWN))pauseChoice=1;
    return;
  }
  if(key=='a'||key=='A'||(key==CODED&&keyCode==LEFT))leftHeld=held;
  if(key=='d'||key=='D'||(key==CODED&&keyCode==RIGHT))rightHeld=held;
}
void dispose() { if(hardware!=null)hardware.disconnect();if(audio!=null)audio.pc.close(); }
