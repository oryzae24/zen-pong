import java.util.ArrayList;

/** Bounded history of ball samples and collision points. */
public class VisualHistory {
  public static final String[] NAMES={"RIPPLES","STRINGS","MANDALA","MESH"};
  public static final int LIMIT=360; // About six seconds at 60 samples per second.
  public static final float RIPPLE_SECONDS=3.5f;
  public static final class Point {
    public final float x,y,time;public final int part;
    Point(float x,float y,float time,int part){this.x=x;this.y=y;this.time=time;this.part=part;}
  }
  public static final class Ring {
    public final float x,y,time;
    Ring(float x,float y,float time){this.x=x;this.y=y;this.time=time;}
  }
  public final ArrayList<Point> trail=new ArrayList<Point>();
  public final ArrayList<Ring> rings=new ArrayList<Ring>();
  public int mode=0,part=0;
  public float clock=0,sampleClock=0;
  public void next(){mode=(mode+1)%NAMES.length;}
  public void clear(){trail.clear();rings.clear();clock=sampleClock=0;part=0;}
  // Do not join the trail across a respawn.
  public void cut(){part++;sampleClock=0;}
  public void tick(float dt){
    // Advance this clock only while playing.
    clock+=dt;
    while(!trail.isEmpty()&&clock-trail.get(0).time>6)trail.remove(0);
    while(!rings.isEmpty()&&clock-rings.get(0).time>RIPPLE_SECONDS)rings.remove(0);
  }
  private void point(float x,float y){
    if(trail.size()>=LIMIT)trail.remove(0);
    trail.add(new Point(x,y,clock,part));sampleClock=0;
  }
  public void sample(float dt,float x,float y){sampleClock+=dt;if(sampleClock>=1f/60)point(x,y);}
  public void impact(float x,float y,float ballX,float ballY){
    // Keep collision samples so the trail retains its corners.
    point(ballX,ballY);
    if(rings.size()>=12)rings.remove(0);
    rings.add(new Ring(x,y,clock));
  }
}
