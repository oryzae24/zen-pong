import java.util.ArrayDeque;

/** One ball, one paddle and three walls. Distances are pixels; time is seconds. */
public final class PongPhysics {
  public static final float LEFT=40, RIGHT=650, TOP=80, BOTTOM=675;
  public static final float BALL_R=10, PADDLE_Y=BOTTOM-45, HALF_WIDTH=80, HALF_HEIGHT=6;
  public static final float[] SPEEDS={220,300,400,550,700}; // Keep the selected speed; never accelerate automatically.
  public float paddleX=345,ballX,ballY,ballVX,ballVY,respawn=0,flash=0;
  public int speedLevel=2,combo=0,best=0;
  public static final class Event {
    public final String kind;public final float x,y;
    Event(String kind,float x,float y){this.kind=kind;this.x=x;this.y=y;}
  }
  public final ArrayDeque<Event> events=new ArrayDeque<Event>();
  public PongPhysics() { serve(); }
  public float speed() { return SPEEDS[speedLevel]; }
  // Change speed without changing direction.
  public void changeSpeed(int direction) {
    int next=Math.max(0,Math.min(SPEEDS.length-1,speedLevel+direction));
    float ratio=SPEEDS[next]/speed();ballVX*=ratio;ballVY*=ratio;speedLevel=next;
  }
  // Serve from above with a small horizontal component.
  public void serve() { ballX=paddleX;ballY=TOP+BALL_R+20;ballVX=speed()*.20f;ballVY=(float)Math.sqrt(speed()*speed()-ballVX*ballVX); }
  public void reset() { paddleX=345;combo=best=0;respawn=flash=0;events.clear();serve(); }
  private void emit(String kind,float x,float y) { if(events.size()<64) events.add(new Event(kind,x,y)); }
  public void update(float dt,float input) {
    if(dt<0||!Float.isFinite(dt)) throw new IllegalArgumentException("dt");
    // Subdivide large steps to prevent collisions from being skipped.
    int count=Math.max(1,(int)Math.ceil(dt*(speed()+650)/(BALL_R*.5f)));
    for(int i=0;i<count;i++) step(dt/count,input);
  }
  private void step(float dt,float input) {
    paddleX=clamp(paddleX+clamp(input,-1,1)*650*dt,LEFT+HALF_WIDTH,RIGHT-HALF_WIDTH);
    flash=Math.max(0,flash-dt);
    if(respawn>0) { respawn-=dt;if(respawn<=0) { respawn=0;serve(); }return; }
    float oldX=ballX,oldY=ballY;
    ballX+=ballVX*dt;ballY+=ballVY*dt;
    // Reflect the velocity component normal to the wall.
    if(ballX-BALL_R<LEFT) { ballX=LEFT+BALL_R;ballVX=Math.abs(ballVX);emit("WALL",LEFT,ballY); }
    if(ballX+BALL_R>RIGHT) { ballX=RIGHT-BALL_R;ballVX=-Math.abs(ballVX);emit("WALL",RIGHT,ballY); }
    if(ballY-BALL_R<TOP) { ballY=TOP+BALL_R;ballVY=Math.abs(ballVY);emit("WALL",ballX,TOP); }
    float contactY=PADDLE_Y-HALF_HEIGHT-BALL_R;
    if(ballVY>0 && oldY<=contactY && ballY>=contactY) {
      // Interpolate the ball position when it crosses the paddle height.
      float x=oldX+(ballX-oldX)*(contactY-oldY)/Math.max(.000001f,ballY-oldY);
      if(Math.abs(x-paddleX)<=HALF_WIDTH+BALL_R) {
        float offset=clamp((x-paddleX)/HALF_WIDTH,-1,1);
        // Return vertically at the center and up to 35 degrees at the edges.
        float angle=offset*(float)Math.toRadians(35);
        ballVX=speed()*(float)Math.sin(angle);ballVY=-speed()*(float)Math.cos(angle);
        ballX=x;ballY=contactY;combo++;best=Math.max(best,combo);flash=.15f;emit("HIT",x,PADDLE_Y-HALF_HEIGHT);
      }
    }
    // Reset the rally after a miss and serve again after 0.8 seconds.
    if(ballY-BALL_R>BOTTOM) { combo=0;respawn=.8f;emit("MISS",ballX,ballY); }
  }
  private static float clamp(float x,float lo,float hi) { return Math.max(lo,Math.min(hi,x)); }
}
