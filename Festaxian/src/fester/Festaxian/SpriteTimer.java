package fester.Festaxian;

/**
 * Created with IntelliJ IDEA.
 * User: RLester
 * Date: 09/05/12
 * Time: 20:11
 * To change this template use File | Settings | File Templates.
 */
public class SpriteTimer {

  private long lastFrameTime = 0;
  // frame delay in ms
  public long delay = 50;

  public boolean Enabled = false;

  // has the timer expired (i.e. delay time has elapsed
  public boolean Expired() {
    if (!Enabled)
      return false;
    if (lastFrameTime == 0) {
      lastFrameTime = System.currentTimeMillis();
      return true;
    }
    else
    if ( ( (System.currentTimeMillis() - lastFrameTime) > delay) )   {
      lastFrameTime = System.currentTimeMillis();
      return true;
    }
    else
      return false;
  }

  public void Reset() {
    lastFrameTime = System.currentTimeMillis();
  }

}
