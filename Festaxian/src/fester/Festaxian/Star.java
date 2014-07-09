package fester.Festaxian;

import android.graphics.Point;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: RLester
 * Date: 09/05/12
 * Time: 19:23
 * To change this template use File | Settings | File Templates.
 */
public class Star extends Sprite {

  public Star() {
    Random r = new Random();
    BlinkAnimation.Frames = new Point[2] ;
    BlinkAnimation.Frames[0] = new Point(0,0);
    BlinkAnimation.Frames[0] = new Point(1,1);
    BlinkAnimation.FrameDelay = r.nextInt(100) + 200;
  }

  public int Color = 0;

  public Animation BlinkAnimation = new Animation();


}
