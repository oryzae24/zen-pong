public class CoreChecks {
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
    System.out.println("Core checks passed: physics, five speed levels, 10 minutes of rallies, bounded visual history.");
  }
}
