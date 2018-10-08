package JavaMultiThread.Chapter4;

import net.jcip.annotations.NotThreadSafe;

/**
 * 可变Point
 */
@NotThreadSafe
public class MutablePoint {
   public int x,y;
   public MutablePoint(){
       x = 0;
       y = 0;
   }
   public MutablePoint(MutablePoint p){
       this.x = p.x;
       this.y = p.y;
   }
}
