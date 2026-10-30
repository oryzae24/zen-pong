// Serial protocol: I,x,y,pause,slower,faster,visual.
class HardwareLink {
  Serial port;
  boolean ready=false, seenInput=false, lastPause=false, lastSlower=false, lastFaster=false, lastVisual=false;
  float axisX=0;
  int selected=-1, openedAt=0, lastInputAt=0, lastHeartbeatAt=0;
  String portName="";
  void cyclePort() {
    String[] names=Serial.list();
    if(names.length==0) { disconnect();return; }
    openPort(names[(selected+1)%names.length]);
  }
  void openPort(String name){
    // Wait for identification and valid input before reporting a connection.
    disconnect();portName=name;
    selected=java.util.Arrays.asList(Serial.list()).indexOf(name);
    try {
      port=new Serial(ZenPong.this,portName,BAUD);
      port.clear(); openedAt=millis(); lastHeartbeatAt=0;
    } catch(Exception e) { port=null; }
  }
  void disconnect() {
    if(port!=null) {
      try { port.write("S,PAUSE\n"); port.stop(); } catch(Exception ignored) { }
    }
    port=null; ready=false; seenInput=false; axisX=0;menuStickReady=false;
    lastPause=lastSlower=lastFaster=lastVisual=false;
  }
  // Discard controller input after 400 ms without a valid packet.
  boolean fresh() { return ready && seenInput && millis()-lastInputAt<400; }
  void update() {
    if(port==null) return;
    try {
      int count=0; // Bound input work per frame to keep rendering responsive.
      while(port.available()>0 && count++<40) {
        String line=port.readStringUntil('\n');
        if(line==null) break;
        receive(trim(line));
      }
      // Send a heartbeat every 200 ms.
      if(ready && millis()-lastHeartbeatAt>=200) {
        send("H"); lastHeartbeatAt=millis();
      }
      if((!ready && millis()-openedAt>6000)||(ready && millis()-lastInputAt>1500)) disconnect();
    } catch(Exception e) { disconnect(); }
  }
  void receive(String line) {
    if(line.equals("READY,ZENPONG,3")) {
      if(!ready) {
        ready=true; lastInputAt=millis();
        state(!started?"TITLE":paused?"PAUSE":"PLAY");
      }
      return;
    }
    if(!ready) return;
    String[] f=split(line,',');
    if(f.length!=7 || !f[0].equals("I")) return;
    try {
      int x=Integer.parseInt(f[1]),y=Integer.parseInt(f[2]);
      int pauseButton=Integer.parseInt(f[3]), slowerButton=Integer.parseInt(f[4]);
      int fasterButton=Integer.parseInt(f[5]),visualButton=Integer.parseInt(f[6]);
      if(abs(x)>1000||abs(y)>1000||pauseButton<0||pauseButton>1||slowerButton<0||slowerButton>1||fasterButton<0||fasterButton>1||visualButton<0||visualButton>1) return;
      axisX=x/1000.0*(INVERT_JOYSTICK_X?-1:1);
      if(seenInput)selectPause(y/1000.0*(INVERT_JOYSTICK_Y?-1:1));
      boolean p=pauseButton==1, s=slowerButton==1, fast=fasterButton==1;
      // Use the first packet as a baseline; react only to subsequent rising edges.
      if(seenInput && p&&!lastPause) togglePlay();
      int speedChange=(fast&&!lastFaster?1:0)-(s&&!lastSlower?1:0);
      if(seenInput && speedChange!=0) game.changeSpeed(speedChange);
      boolean visual=visualButton==1;
      if(seenInput && visual&&!lastVisual)effects.next();
      lastPause=p; lastSlower=s; lastFaster=fast; lastVisual=visual;seenInput=true;lastInputAt=millis();
    } catch(NumberFormatException ignored) { }
  }
  void send(String command) {
    if(port!=null && ready) {
      try { port.write(command+"\n"); }
      catch(Exception e) { disconnect(); }
    }
  }
  void state(String mode) { send("S,"+mode); }
  void toneNote(int hz,int duration) { send("T,"+hz+","+duration); }
}
