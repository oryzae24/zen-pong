public class PongChecks {
  static void check(boolean c,String m) { if(!c)throw new AssertionError(m); }
  static void near(float a,float b,String m) { check(Math.abs(a-b)<.1f,m+": "+a+" != "+b); }
  static float norm(PongPhysics p) { return (float)Math.hypot(p.ballVX,p.ballVY); }
  static PongPhysics hit(float offset) {
    PongPhysics p=new PongPhysics();p.ballX=p.paddleX+offset;p.ballY=PongPhysics.PADDLE_Y-PongPhysics.HALF_HEIGHT-PongPhysics.BALL_R-1;
    p.ballVX=0;p.ballVY=p.speed();p.update(.01f,0);return p;
  }
  public static void main(String[] args) {
    for(float offset:new float[]{-80,0,80}) {
      PongPhysics p=hit(offset);check(p.combo==1&&p.ballVY<0,"board contact returns upward");
      near(norm(p),p.speed(),"reflection retains selected speed");
      check(Math.abs(p.ballVX)<=p.speed()*Math.sin(Math.toRadians(35))+.1,"angle bounded");
      if(offset==0)near(p.ballVX,0,"centre returns vertically");
      else check(Math.signum(p.ballVX)==Math.signum(offset),"edge sets reflection direction");
    }
    PongPhysics p=new PongPhysics();float ratio=p.ballVX/p.ballVY;p.changeSpeed(1);
    near(norm(p),p.speed(),"speed changes immediately");near(p.ballVX/p.ballVY,ratio,"speed change preserves direction");
    for(int i=0;i<20;i++)p.changeSpeed(1);check(p.speedLevel==PongPhysics.SPEEDS.length-1,"maximum speed");
    for(int i=0;i<20;i++)p.changeSpeed(-1);check(p.speedLevel==0,"minimum speed");
    p.ballX=PongPhysics.LEFT+1;p.ballVX=-p.speed();p.ballVY=0;p.update(.01f,0);
    check(p.ballVX>0,"left wall");near(norm(p),p.speed(),"wall preserves speed");
    p.ballX=PongPhysics.RIGHT-1;p.ballVX=p.speed();p.update(.01f,0);check(p.ballVX<0,"right wall");
    p.ballY=PongPhysics.TOP+1;p.ballVX=0;p.ballVY=-p.speed();p.update(.01f,0);check(p.ballVY>0,"top wall");
    p.combo=5;p.ballY=PongPhysics.BOTTOM+30;p.ballVY=p.speed();p.update(.01f,0);
    check(p.respawn>0&&p.combo==0,"miss resets rally");p.update(1,0);
    check(p.respawn==0&&p.ballY<300,"respawns from top");
    p.update(3,1);near(p.paddleX,PongPhysics.RIGHT-PongPhysics.HALF_WIDTH,"paddle right limit");
    p.update(3,-1);near(p.paddleX,PongPhysics.LEFT+PongPhysics.HALF_WIDTH,"paddle left limit");
    for(int level=0;level<PongPhysics.SPEEDS.length;level++) {
      PongPhysics rally=new PongPhysics();rally.changeSpeed(level-rally.speedLevel);rally.ballVX=0;rally.ballVY=rally.speed();
      for(int i=0;i<240*120;i++){rally.update(1f/240,0);check(rally.respawn==0,"central rally never misses");}
      check(rally.combo>10,"long rally at level "+level);near(norm(rally),rally.speed(),"no automatic acceleration");
    }
    ZenPong s=new ZenPong();s.hardware=s.new HardwareLink();s.pcSound=false;s.audio=s.new GameAudio();
    try {
      s.hardware.receive("READY,ZENPONG,3");s.hardware.receive("I,0,0,0,0,0,0");
      s.hardware.receive("I,0,0,1,0,0,0");check(s.started&&!s.paused,"joystick starts");
      s.hardware.receive("I,0,0,1,0,0,0");check(!s.paused,"held pause ignored");
      s.hardware.receive("I,0,0,0,0,0,0");s.hardware.receive("I,0,0,1,0,0,0");check(s.paused,"second press pauses");
      s.hardware.receive("I,1000,0,0,0,1,0");check(s.game.speedLevel==3&&s.hardware.axisX==1,"speed button and movement");
      s.hardware.receive("I,1000,0,0,0,1,0");check(s.game.speedLevel==3,"held speed ignored");
      s.hardware.receive("I,0,0,0,0,0,0");s.hardware.receive("I,0,0,0,1,1,0");check(s.game.speedLevel==3,"simultaneous buttons cancel");
      s.hardware.receive("I,2000,0,0,1,0,0");check(s.game.speedLevel==3,"invalid packet ignored");
      s.key='-';s.keyPressed();s.keyPressed();check(s.game.speedLevel==2,"keyboard repeat blocked");s.keyReleased();
      float oldX=s.game.ballX,oldY=s.game.ballY,oldVX=s.game.ballVX,oldVY=s.game.ballVY;
      s.hardware.receive("I,0,0,0,0,0,1");check(s.effects.mode==1,"third button changes visual");
      s.hardware.receive("I,0,0,0,0,0,1");check(s.effects.mode==1,"held visual button does not repeat");
      for(int i=1;i<VisualHistory.NAMES.length;i++){s.hardware.receive("I,0,0,0,0,0,0");s.hardware.receive("I,0,0,0,0,0,1");}
      check(s.effects.mode==0,"four visual modes wrap around");
      s.key='v';s.keyPressed();s.keyPressed();check(s.effects.mode==1,"V autorepeat blocked");
      s.keyReleased();s.keyPressed();s.keyReleased();check(s.effects.mode==2,"V after release changes mode");
      near(s.game.ballX,oldX,"mode does not move ball x");near(s.game.ballY,oldY,"mode does not move ball y");
      near(s.game.ballVX,oldVX,"mode does not change x velocity");near(s.game.ballVY,oldVY,"mode does not change y velocity");
      s.hardware.receive("I,1000,0,0,0,0,2");check(s.effects.mode==2,"invalid visual input ignored");
      s.hardware.receive("I,1000,0,0,0");check(s.hardware.axisX==0,"old input format ignored");
      s.key=']';float volume=s.pcVolume;s.keyPressed();check(s.pcVolume>volume,"PC volume increases");
      s.key='[';s.keyPressed();near(s.pcVolume,volume,"PC volume decreases");
      s.game.combo=7;s.paused=true;s.mouseButton=s.LEFT;s.mouseY=Math.round(s.overlayCenterY()+20);
      s.mouseX=10;s.mousePressed();check(s.paused&&s.game.combo==7,"click outside pause buttons is ignored");
      float resumeX=s.game.ballX,resumeY=s.game.ballY;
      s.mouseX=280;s.mousePressed();check(s.started&&!s.paused&&s.game.combo==7,"resume button keeps the rally");
      near(s.game.ballX,resumeX,"resume keeps ball x");near(s.game.ballY,resumeY,"resume keeps ball y");
      s.setPaused(true);s.effects.sample(1f/60,100,200);s.leftHeld=true;s.accumulator=1;s.audio.effect("HIT");
      int savedSpeed=s.game.speedLevel,savedMode=s.effects.mode;
      s.mouseX=280;s.mouseY=Math.round(s.overlayCenterY()+50);s.mouseButton=s.RIGHT;s.mousePressed();check(s.paused,"right click cannot leave pause");
      s.mouseButton=s.LEFT;s.mousePressed();
      check(!s.started&&!s.paused&&s.game.combo==0&&s.effects.trail.isEmpty(),"title button resets rally and marks");
      check(s.game.speedLevel==savedSpeed&&s.effects.mode==savedMode,"title return keeps speed and visual selection");
      check(!s.leftHeld&&s.accumulator==0&&!s.audio.active,"title return clears held input, time and effects audio");
      s.hardware.receive("I,0,0,1,0,0,0");check(s.started&&!s.paused,"controller can start again from title");
      s.key='r';s.keyPressed();s.game.combo=3;s.keyPressed();check(s.game.combo==3,"held reset key does not reset repeatedly");s.keyReleased();
    } finally { s.audio.pc.close(); }
    System.out.println("Zen Pong checks passed: reflections, constant speed, five levels, limits, respawn, 10 minutes of rallies, serial buttons, four visuals, keyboard audio and pause menu navigation.");
  }
}
