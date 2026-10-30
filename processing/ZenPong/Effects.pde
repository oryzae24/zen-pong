// Render four visual modes from the same ball history.
class VisualEffects extends VisualHistory {
  int[] palette={0xff58dafa,0xffbc88ff,0xffff76c5,0xffffbb73};
  void render(){
    pushStyle();noFill();strokeCap(ROUND);strokeJoin(ROUND);
    // Clip effects to the court.
    clip(PongPhysics.LEFT,PongPhysics.TOP,PongPhysics.RIGHT-PongPhysics.LEFT,PongPhysics.BOTTOM-PongPhysics.TOP);
    if(mode==0)ripples();else if(mode==1)strings();else if(mode==2)mandala();else mesh();
    noClip();popStyle();
  }
  float fade(Point p,float life){return constrain(1-(clock-p.time)/life,0,1);}
  int shade(float phase){
    float t=phase%palette.length;int i=floor(t);
    return lerpColor(palette[i],palette[(i+1)%palette.length],t-i);
  }
  // Expanding rings at collision points.
  void ripples(){
    strokeWeight(1.5);
    for(Ring r:rings){
      float age=clock-r.time,f=constrain(1-age/RIPPLE_SECONDS,0,1);
      for(int j=0;j<4;j++){
        float radius=age*180-j*24;
        if(radius>0){stroke(shade(r.time*.2+j*.6),155*f/(1+j*.5));circle(r.x,r.y,radius*2);}
      }
    }
  }
  // Connect samples from different times.
  void strings(){
    strokeWeight(1);
    for(int i=60;i<trail.size();i+=3){
      Point a=trail.get(i-60),b=trail.get(i),m=trail.get(i-30);
      float area=abs((m.x-a.x)*(b.y-a.y)-(m.y-a.y)*(b.x-a.x));
      if(a.part==b.part&&area>250){stroke(shade(b.time*.28+(float)i/max(1,trail.size())*.6),82*fade(a,6));line(a.x,a.y,b.x,b.y);}
    }
  }
  // Repeat the trail at 60-degree intervals.
  void mandala(){
    float cx=(PongPhysics.LEFT+PongPhysics.RIGHT)/2,cy=(PongPhysics.TOP+PongPhysics.PADDLE_Y)/2;
    pushMatrix();translate(cx,cy);
    for(int copy=0;copy<6;copy++){
      pushMatrix();rotate(copy*TWO_PI/6);
      for(int contour=0;contour<2;contour++){
        float scale=.56+contour*.035;strokeWeight(1);
        for(int i=max(1,trail.size()-240);i<trail.size();i++){
          Point a=trail.get(i-1),b=trail.get(i);
          if(a.part!=b.part)continue;
          stroke(shade(copy/3.0+contour*.45+b.time*.15),(72-contour*20)*fade(a,6));
          line((a.x-cx)*scale,(a.y-cy)*scale,(b.x-cx)*scale,(b.y-cy)*scale);
        }
      }
      popMatrix();
    }
    popMatrix();
  }
  // Deform nearby grid points more strongly.
  PVector meshPoint(float x,float y){
    float dx=x-game.ballX,dy=y-game.ballY,r2=dx*dx+dy*dy;
    float f=game.respawn>0?0:exp(-r2/23000),pull=f*(.44+.08*sin(clock*4-sqrt(r2)*.04));
    return new PVector(x-dx*pull-dy*f*.22,y-dy*pull+dx*f*.22,f);
  }
  void mesh(){
    float padding=112; // Generate four extra cells beyond each edge to hide the mesh boundary.
    strokeWeight(1);
    for(int axis=0;axis<2;axis++){
      float fixedStart=axis==0?PongPhysics.LEFT:PongPhysics.TOP,fixedEnd=axis==0?PongPhysics.RIGHT:PongPhysics.BOTTOM;
      float start=axis==0?PongPhysics.TOP:PongPhysics.LEFT,end=axis==0?PongPhysics.BOTTOM:PongPhysics.RIGHT;
      for(float fixed=fixedStart+8-padding;fixed<fixedEnd+padding;fixed+=28){
        PVector previous=null;
        for(float v=start+8-padding;v<end+padding;v+=14){
          PVector p=axis==0?meshPoint(fixed,v):meshPoint(v,fixed);
          if(previous!=null){stroke(shade(clock*.15+(1-p.z)*.9),22+112*p.z);line(previous.x,previous.y,p.x,p.y);}
          previous=p;
        }
      }
    }
  }
}
