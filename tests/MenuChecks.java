public class MenuChecks {
  static void check(boolean c,String m){if(!c)throw new AssertionError(m);}
  static void frame(ZenPong s,int x,int y,int pressed){s.hardware.receive("I,"+x+","+y+","+pressed+",0,0,0");}
  public static void main(String[] args){
    ZenPong s=new ZenPong();s.hardware=s.new HardwareLink();s.pcSound=false;s.audio=s.new GameAudio();
    try{
      s.hardware.receive("READY,ZENPONG,2");check(!s.hardware.ready,"one-axis firmware cannot connect");
      s.hardware.receive("READY,ZENPONG,3");frame(s,0,1000,0);frame(s,0,1000,1);
      check(s.started&&!s.paused,"controller starts from title");
      frame(s,0,1000,0);frame(s,0,1000,1);frame(s,0,1000,1);
      check(s.paused&&s.pauseChoice==0,"pause defaults to resume despite held stick and button");
      frame(s,1000,0,0);check(s.pauseChoice==0,"horizontal axis does not select vertical menu");
      s.hardware.lastInputAt=s.millis()-100;int receivedAt=s.hardware.lastInputAt;
      frame(s,0,2000,0);check(s.pauseChoice==0&&s.hardware.lastInputAt==receivedAt,"invalid Y is rejected");
      frame(s,0,1000,0);check(s.pauseChoice==1,"down selects title return");
      frame(s,0,-1000,0);check(s.pauseChoice==1,"stick must return to neutral before another selection");
      frame(s,0,0,0);frame(s,0,-1000,0);check(s.pauseChoice==0,"up selects resume");
      s.game.combo=8;float x=s.game.ballX,y=s.game.ballY;frame(s,0,0,1);
      check(!s.paused&&s.started&&s.game.combo==8&&s.game.ballX==x&&s.game.ballY==y,"press resumes without resetting rally");
      frame(s,0,0,1);check(!s.paused,"held confirmation does not pause again");
      frame(s,0,0,0);frame(s,0,0,1);frame(s,0,0,0);frame(s,0,1000,1);
      check(!s.started&&!s.paused&&s.game.combo==0,"down and press return to title");
      frame(s,0,1000,1);check(!s.started,"held confirmation does not start again");
      frame(s,0,0,0);frame(s,0,0,1);check(s.started,"released button can start another rally");
      s.setPaused(true);s.key=(char)s.CODED;s.keyCode=s.DOWN;s.keyPressed();s.keyReleased();
      check(s.pauseChoice==1,"down arrow selects title return");
      s.key='\n';s.keyPressed();s.keyPressed();check(!s.started,"Enter confirms once even when held");s.keyReleased();
      s.keyPressed();s.keyReleased();check(s.started,"Enter starts after release");
      s.setPaused(true);s.key='s';s.keyPressed();s.keyReleased();s.key='w';s.keyPressed();s.keyReleased();
      check(s.pauseChoice==0,"W/S select the two menu entries");
      s.key='p';s.keyPressed();s.keyPressed();check(!s.paused,"held P resumes only once");s.keyReleased();
      s.keyPressed();s.keyPressed();check(s.paused,"held P pauses only once");s.keyReleased();
    }finally{s.audio.pc.close();}
    System.out.println("Menu checks passed: vertical joystick selection, neutral gating, press confirmation, resume, title return, held-button protection and keyboard navigation.");
  }
}
