// Share note pitches and durations between the buzzer and PC audio.
class GameAudio {
  PcTone pc=new PcTone();
  int[] frequencies, lengths;
  int index=0, nextNoteAt=0, nextMelodyAt=0;
  boolean active=false;
  final int[] melodyHz={523,659,784,659,587,698,880,0,784,659,587,523,0};
  final int[] melodyMs={150,150,220,150,150,150,260,90,150,150,150,300,400};
  GameAudio() { pc.start(); }
  void note(int hz,int duration) {
    hardware.toneNote(hz,duration);
    if(pcSound && hz>0) pc.play(hz,duration); else pc.silence();
  }
  void sequence(int[] hz,int[] ms) {
    frequencies=hz; lengths=ms; index=0; nextNoteAt=millis(); active=true;
  }
  void effect(String kind) {
    if(kind.equals("WALL") && active) return; // Do not interrupt a hit or miss with a wall sound.
    if(kind.equals("HIT")) sequence(new int[]{659,880},new int[]{35,65});
    else if(kind.equals("MISS")) sequence(new int[]{392,523},new int[]{130,240});
    else if(kind.equals("START")) sequence(new int[]{523,659,784},new int[]{75,75,140});
    else if(kind.equals("WALL")) sequence(new int[]{330},new int[]{35});
  }
  void stop() {
    active=false; pc.silence(); hardware.toneNote(0,0); nextMelodyAt=millis()+700;
  }
  void update() {
    // Schedule notes without blocking rendering.
    int now=millis();
    if(!active && !started && now>=nextMelodyAt) sequence(melodyHz,melodyMs);
    if(active && now>=nextNoteAt) {
      if(index>=frequencies.length) {
        active=false; nextMelodyAt=now+500; return;
      }
      int duration=lengths[index];
      note(frequencies[index],max(1,round(duration*0.86)));
      nextNoteAt=now+duration; index++;
    }
  }
}
// Generate PC audio on a separate thread.
class PcTone extends Thread {
  volatile boolean running=true;
  volatile int frequency=0;
  volatile long endAt=0;
  SourceDataLine output;
  void play(int hz,int ms) { frequency=hz; endAt=System.nanoTime()+ms*1000000L; }
  void silence() { endAt=0; frequency=0; }
  void close() {
    running=false; silence();
    if(output!=null) { output.stop(); output.close(); }
  }
  public void run() {
    try {
      AudioFormat format=new AudioFormat(22050,16,1,true,false);
      output=AudioSystem.getSourceDataLine(format);
      output.open(format,2048); output.start();
      byte[] buffer=new byte[512]; double phase=0, gain=0;
      while(running) {
        int hz=frequency;
        long deadline=endAt;
        for(int i=0;i<256;i++) {
          boolean audible=hz>0 && System.nanoTime()+i*1000000000L/22050<deadline;
          double target=audible?0.12*pcVolume:0;
          gain+=(target-gain)*0.06; // Smooth amplitude changes to reduce clicks.
          phase+=hz/22050.0; phase-=Math.floor(phase);
          short sample=(short)((phase<0.5?1:-1)*gain*32767);
          buffer[i*2]=(byte)(sample&255); buffer[i*2+1]=(byte)((sample>>8)&255);
        }
        output.write(buffer,0,buffer.length);
      }
    } catch(Exception e) {
      if(running) println("PC audio unavailable: "+e.getMessage());
    } finally { if(output!=null) { output.stop(); output.close(); } }
  }
}
