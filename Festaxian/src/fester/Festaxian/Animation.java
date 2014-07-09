package fester.Festaxian;

import android.graphics.Point;

/**
 * Created with IntelliJ IDEA.
 * User: rlester
 * Date: 20/04/12
 * Time: 19:08
 * To change this template use File | Settings | File Templates.
 */
public class Animation {

  private long lastFrameTime = 0;

  // array of points(sprite tiles) within the sprite sheet
  public Point[] Frames;
  // frame delay in ms
  public long FrameDelay = 50;
  public int FrameNo = 0;

  public boolean Loop = true;
  public boolean Complete = false;

  // Advance the animation to the next frame
  // based on the time elapsed since the last frame change
  public void AdvanceFrame() {
    if (lastFrameTime == 0)
      lastFrameTime = System.currentTimeMillis();
    else
    if ( (System.currentTimeMillis() - lastFrameTime) > FrameDelay)  {
      lastFrameTime = System.currentTimeMillis();
      if (!Complete)
        if (FrameNo < Frames.length-1)
          FrameNo++;
        else
          if (Loop)
            FrameNo =0;
          else {
            Complete = true ;
          }
    }
  }

  public void Reset() {
    FrameNo =0;
    Complete = false;
  }

  // Get the current frame
  public Point GetCurrentFrame() {
    return Frames[FrameNo]  ;
  }

  // Get a specific frame
  public Point GetFrame(int frameNo) {
    return Frames[frameNo]  ;
  }

}
