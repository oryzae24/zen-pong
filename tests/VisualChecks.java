public class VisualChecks {
  static void check(boolean c,String m){if(!c)throw new AssertionError(m);}
  public static void main(String[] args){
    VisualHistory h=new VisualHistory();
    check(VisualHistory.NAMES.length==4,"four visual modes");
    for(int i=0;i<VisualHistory.NAMES.length;i++)h.next();check(h.mode==0,"four modes wrap");
    h.impact(40,200,50,200);h.tick(2);check(h.rings.size()==1,"wide ripples remain visible after two seconds");
    h.tick(VisualHistory.RIPPLE_SECONDS-2+.01f);check(h.rings.isEmpty(),"ripples expire after their extended lifetime");h.clear();
    h.next();h.sample(1f/60,100,200);int first=h.trail.get(0).part;
    h.cut();h.sample(1f/60,300,120);
    check(first!=h.trail.get(1).part,"respawn breaks the drawn line");
    for(int i=0;i<2000;i++){h.tick(1f/240);h.sample(1f/240,i%600,200);h.impact(40,200,50,200);}
    check(h.trail.size()<=VisualHistory.LIMIT&&h.rings.size()<=12,"history stays bounded");
    h.tick(7);check(h.trail.isEmpty()&&h.rings.isEmpty(),"old marks expire");
    h.sample(1f/60,100,200);float stamp=h.trail.get(0).time;h.next();
    check(h.trail.get(0).time==stamp,"mode switch preserves history and clock");
    int mode=h.mode;h.clear();check(h.mode==mode&&h.clock==0&&h.trail.isEmpty(),"reset clears marks and preserves mode");
    ZenPong s=new ZenPong();s.hardware=s.new HardwareLink();s.pcSound=false;s.audio=s.new GameAudio();
    try{
      s.hardware.receive("READY,ZENPONG,1");check(!s.hardware.ready,"old firmware rejected");
      s.hardware.receive("READY,ZENPONG,3");s.hardware.receive("I,0,0,0,0,0,1");
      check(s.hardware.fresh(),"valid controller input is fresh");
      s.hardware.lastInputAt=s.millis()-401;check(!s.hardware.fresh(),"stale controller input is not connected");
      s.hardware.receive("I,0,0,0,0,0,1");check(s.hardware.fresh(),"new input restores connected status");
      check(s.effects.mode==0,"connecting with button held does not cycle");
      s.hardware.receive("I,0,0,0,0,0,0");s.hardware.receive("I,0,0,0,0,0,1");check(s.effects.mode==1,"release then press cycles");
      s.game.ballX=200;s.game.ballY=PongPhysics.TOP+1;s.game.ballVX=0;s.game.ballVY=-400;
      s.stepGame(1f/240,0);
      check(!s.effects.rings.isEmpty()&&s.effects.rings.get(0).y==PongPhysics.TOP,"wave originates at actual wall contact");
      s.game.ballY=PongPhysics.BOTTOM+30;s.game.ballVY=400;s.stepGame(1f/240,0);
      check(s.effects.part==1,"real miss separates trail segments");
    }finally{s.audio.pc.close();}
    System.out.println("Visual checks passed: mode cycling, input debounce, no gameplay changes, bounded history, expiry, collision origin and respawn separation.");
  }
}
