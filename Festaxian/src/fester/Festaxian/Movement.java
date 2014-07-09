package fester.Festaxian;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.Rect;

/**
 * Created with IntelliJ IDEA.
 * User: rlester
 * Date: 20/04/12
 * Time: 21:28
 * To change this template use File | Settings | File Templates.
 */
public class Movement {

  private long lastFrameTime = 0;
  private double movementPixelSize = 4;
  private double movementStep = 1;

  private SpriteTimer xTimer = new SpriteTimer();
  public int XDir = 0; // Don't move in the x plane

  // array of points(sprite tiles) within the sprite sheet
  public Point[] Frames;
  // frame delay in ms
  public long delay = 50;
  public long xDelay = 200;

  public boolean Loop = true;
  public boolean Complete = false;

  public Point BezierStart ;
  public Point BezierCP1 ;
  public Point BezierCP2 ;
  public Point BezierEnd ;
  private float bezierCurPos;
  public Rect Extent;
  public int XOffset = 0;
  public int YOffset = 0;
  public double PixelSize = 4;

  public Movement() {
    xTimer.delay = xDelay;
    xTimer.Enabled = true;
  }

  public MovementDirection Direction = MovementDirection.Down;

  public MovementStyle Style = MovementStyle.Limit;

  public void Move(Point currentlocation, Rect extent, Rect spriteRect, double scale) {
    Move(currentlocation, extent, spriteRect, scale, false) ;
  }

  // Can a move take place (i.e. delay time has elapsed
  public boolean CanMove() {
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

  // Advance the movement to the next step
  // based on the time elapsed since the last frame change
  public void Move(Point currentlocation, Rect extent, Rect spriteRect, double scale, boolean moveNow) {
    if (lastFrameTime == 0 && !moveNow)
      lastFrameTime = System.currentTimeMillis();
    else
    if ( ((System.currentTimeMillis() - lastFrameTime) > delay) || moveNow)  {
      lastFrameTime = System.currentTimeMillis();

      boolean moveOK = true;

      switch(Direction) {
        case Down:
          if (currentlocation.y <= (extent.bottom-((int)(spriteRect.height()*scale))-movementPixelSize ))
            currentlocation.y = currentlocation.y + (int)(movementPixelSize * movementStep);
          else
            switch(Style){
              case Cyclic: Direction =  MovementDirection.Up;
              break;
              case Wrap: currentlocation.y = extent.top;
              case Limit: Complete = true;
              break;
            }
          break;
        case Up:
          if (currentlocation.y >= extent.top)
            currentlocation.y =  currentlocation.y - (int)(movementPixelSize * movementStep);
          else
            if (Style == MovementStyle.Cyclic)
              Direction =  MovementDirection.Down;
            else
              Complete = true;
          break;
        case Left:
          if (currentlocation.x >= extent.left)
            currentlocation.x =  currentlocation.x - (int)(movementPixelSize * movementStep);
          else
            if (Style == MovementStyle.Cyclic)
              Direction =  MovementDirection.Right;
            else
              Complete = true;
          break;
        case Right:
          if (currentlocation.x <= (extent.right-((int)(spriteRect.width()*scale))-movementPixelSize ))
            currentlocation.x =  currentlocation.x + (int)(movementPixelSize * movementStep);
          else
            if (Style == MovementStyle.Cyclic)
              Direction =  MovementDirection.Left;
            else
              Complete = true;
          break;
        case Bezier: {
          Point newPos =  MoveBezierPath();
          if (newPos.x != -1000) {
            currentlocation.x = newPos.x;
            currentlocation.y = newPos.y;
          }
          else {
            // reset the curve position
            bezierCurPos =0;
            // mark movement cycle as complete
            Complete = true;
          }

        }
          break;
      }
    }

    if (xTimer.Expired())
      currentlocation.x =  currentlocation.x + (XDir * (int)(movementPixelSize * movementStep) );
  }

  public double getMovementPixelSize() {
    return movementPixelSize;
  }

  public void setMovementPixelSize(int movementPixelSize) {
    this.movementPixelSize = movementPixelSize;
  }

  public double getMovementStep() {
    return movementStep;
  }

  public void setMovementStep(double movementStep) {
    this.movementStep = movementStep;
  }

  public enum MovementDirection {
    Down,
    Up,
    Left,
    Right,
    Bezier
  }

  public enum MovementStyle {
    Cyclic,
    Limit,
    Wrap,
    Bezier
  }

  public void CreateBezierPath(Point s, Point c1, Point c2, Point e) {
    bezierPath = new Path();
    // Set the current flight path position increment to zero
    bezierCurPos = 0;
    // Start at the current position
    bezierPath.moveTo(s.x, s.y);
    // Create the take-off path for the dive path
    bezierPath.cubicTo(s.x-(int)(PixelSize*8), s.y-(int)(PixelSize*16), s.x-(int)(PixelSize*8), s.y-(int)(PixelSize*16), s.x-(int)(PixelSize*32), s.y);
    // Create the main flight path towards the player position
    bezierPath.cubicTo(c1.x, c1.y, c2.x, c2.y, e.x, e.y);
    //bezierPath.cubicTo(c1.x, c1.y, c2.x, c2.y, e.x, e.y);
    // Finish off diving straight down past the bottom of the extent
    bezierPath.lineTo(e.x, this.Extent.bottom);
    // Get the total length so we can detect the end of the flight
    PathMeasure measure = new PathMeasure();
    measure.setPath(bezierPath, false);
    bezierTotalLength = (int)measure.getLength();
  }

  private Point GetPointAtDistance(float distance) {
    Point newPos ;
    Point tanPos ;
    double angle ;
    PathMeasure measure = new PathMeasure();
    measure.setPath(bezierPath, false);
    float length = measure.getLength();
    float pos[] = new float[2];
    float tan[] = new float[2];
    measure.getPosTan(distance, pos, tan) ;

    newPos = new Point((int)pos[0], (int)pos[1]) ;
    // Calculate the angle of attack in degrees
    attackAngle = Math.atan2(tan[1], tan[0])*180/Math.PI;
    return newPos  ;
  }

  private Path bezierPath;
  private int bezierTotalLength = 0;
  // Attack angle is calculated by the tangent at the current
  // poistion on the bezier curve. Use this for rotation purposes
  public double attackAngle ;

  private Point MoveBezierPath() {
    if(bezierCurPos < bezierTotalLength) {
      bezierCurPos += movementStep * movementPixelSize;
      return GetPointAtDistance(bezierCurPos);
    }
    else {
      bezierCurPos = 0;
      return new Point(-1000,-1000) ;
    }
  }


}


