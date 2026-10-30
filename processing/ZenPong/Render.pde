// Draw the court, game state and pause menu.
void renderGame() {
  background(15,23,40);fill(242,246,255);textSize(28);text("ZEN PONG",38,54);
  pushMatrix();translate(8,35);scale(.78);
  noStroke();fill(23,37,56);rect(PongPhysics.LEFT,PongPhysics.TOP,PongPhysics.RIGHT-PongPhysics.LEFT,PongPhysics.BOTTOM-PongPhysics.TOP);
  effects.render();
  stroke(99,231,213);strokeWeight(5);
  line(PongPhysics.LEFT,PongPhysics.BOTTOM,PongPhysics.LEFT,PongPhysics.TOP);
  line(PongPhysics.LEFT,PongPhysics.TOP,PongPhysics.RIGHT,PongPhysics.TOP);
  line(PongPhysics.RIGHT,PongPhysics.TOP,PongPhysics.RIGHT,PongPhysics.BOTTOM);
  noStroke();fill(game.flash>0?color(255,222,153):color(99,231,213));
  rect(game.paddleX-PongPhysics.HALF_WIDTH,PongPhysics.PADDLE_Y-PongPhysics.HALF_HEIGHT,2*PongPhysics.HALF_WIDTH,2*PongPhysics.HALF_HEIGHT,6);
  if(game.respawn<=0) { fill(255,222,153);circle(game.ballX,game.ballY,PongPhysics.BALL_R*2); }
  popMatrix();fill(143,160,185);textSize(14);text("RALLY",560,120);
  fill(240,247,255);textSize(56);text(nf(game.combo,2),557,181);
  fill(143,160,185);textSize(14);text("BEST  "+game.best,560,216);
  text("SPEED",560,290);fill(240,247,255);textSize(24);text((game.speedLevel+1)+" / "+PongPhysics.SPEEDS.length,560,327);
  fill(143,160,185);textSize(14);text("VISUAL",560,391);fill(240,247,255);textSize(18);text(VisualHistory.NAMES[effects.mode],560,421);
  fill(143,160,185);textSize(14);text("CONTROLLER",560,488);
  boolean connected=hardware.fresh(),waiting=!connected&&hardware.port!=null;
  int statusColor=connected?color(99,231,213):waiting?color(255,190,107):color(143,160,185);
  stroke(statusColor);strokeWeight(1.5);if(connected)fill(statusColor);else noFill();circle(564,519,8);noStroke();
  fill(statusColor);text(connected?"CONNECTED":waiting?"WAITING":"OFFLINE",575,524);
  fill(143,160,185);textSize(12);text(connected||waiting?hardware.portName:"KEYBOARD",700,524);
  if(!started||paused) {
    float centerY=overlayCenterY();
    fill(10,18,30,220);rect(120,centerY-(paused?88:45),320,paused?184:90,12);fill(99,231,213);textAlign(CENTER);textSize(32);
    text(started?"PAUSED":"ZEN PONG",280,centerY+(paused?-44:13));
    if(paused){
      for(int i=0;i<2;i++){
        boolean selected=pauseChoice==i;
        if(selected){stroke(99,231,213);strokeWeight(1.5);}else noStroke();
        fill(selected?color(36,76,85):color(35,59,76));rect(170,centerY-14+i*46,220,36,6);noStroke();
        fill(selected?color(99,231,213):color(240,247,255));textSize(14);text(i==0?"RESUME":"TITLE",280,centerY+9+i*46);
      }
    }
    textAlign(LEFT);
  }
}
float overlayCenterY(){return 35+.78*(PongPhysics.TOP+PongPhysics.BOTTOM)/2;}
