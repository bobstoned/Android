package fester.Festaxian;

import android.graphics.*;

public class Sprite {

  public Point Position;
  public Rect SpriteRect;
  public Rect[] MultipleSpriteRect ;
  public Rect Extent ;
  public boolean Drawing = false;
  // Array of sprites used for animation
  public Animation Animation = new Animation();
  // Death animation/explosion etc.
  public Animation DeathAnimation  = new Animation();
  public Movement Movement = new Movement();
  public boolean Dead = false;
  public boolean Dying = false;
  private double pixelSize = 1;
  // The logical width of the sprite before scaling (disregard the actual bitmap dimensions)
  public int logicalWidth  ;
  // The logical height of the sprite before scaling (disregard the actual bitmap dimensions)
  public int logicalHeight ;
  public void setPixelSize(double value)  {
    pixelSize= value;
    this.Movement.PixelSize = pixelSize;
  }

  public double PixelSize()  {
    return pixelSize;
  }

  public Point GetFrame() {
    if (!Dying)
      return Animation.Frames[Animation.FrameNo];
    else
      return DeathAnimation.Frames[DeathAnimation.FrameNo];
  }

  public void AdvanceFrame() {
    if (!Dead)
      if (!Dying)
        Animation.AdvanceFrame();
      else  {
        DeathAnimation.AdvanceFrame();
        if (DeathAnimation.Complete) {
          Dying = false;
          Dead = true;
          DeathAnimation.Complete = false;
        }
      }
  }

  public void Move(boolean moveNow) {
    if (!IsDyingOrDead())
      Movement.Move(this.Position, this.Extent, this.getLogicalRect(), pixelSize, moveNow);
  }

  public void DrawBoundingBox(Canvas canvas, int color) {
    Rect posRect = new Rect(Position.x, Position.y, Position.x+( (int)(getLogicalRect().width()*pixelSize))-1,  Position.y+((int)(getLogicalRect().height()*pixelSize))-1);
    Paint paint = new Paint();
    paint.setStyle(Paint.Style.STROKE);
    paint.setColor(color);
    paint.setStrokeWidth(1);
    canvas.drawRect(posRect, paint);
  }

  public void DrawCollisionBoxes(Canvas canvas, int color) {
    Rect box ;
    Paint paint = new Paint();
    paint.setStyle(Paint.Style.STROKE);
    paint.setColor(color);
    paint.setStrokeWidth(1);
    for(int i=0; i<MultipleSpriteRect.length;i++) {
      box = GetBoundingBox( MultipleSpriteRect[i]) ;
      canvas.drawRect(box, paint);
    }
  }

  public Rect GetBoundingBox() {
    Rect posRect = new Rect(Position.x, Position.y, Position.x+((int)(getLogicalRect().width()*pixelSize))-1, Position.y+((int)(getLogicalRect().height()*pixelSize))-1);
    return posRect;
  }

  public Rect GetBoundingBox(Rect rect) {
    int left = Position.x+(int)(rect.left * pixelSize);
    int top = Position.y + (int)(rect.top * pixelSize);
    int right =  left + ((int)(rect.width()*pixelSize))-1;
    int bottom = top + ((int)(rect.height()*pixelSize))-1;

    Rect posRect = new Rect(left, top, right, bottom);
    return posRect;
  }

  // Compares the supplied "rect" value with the bounding rect(s) of
  // this sprite
  public boolean IsCollision(Rect rect) {
    // Create a Rect representing the current position
    if (MultipleSpriteRect == null)
      return rect.intersect(GetBoundingBox()) ;
    else  {
      for(int i=0; i<MultipleSpriteRect.length;i++)
        if (rect.intersect(GetBoundingBox(MultipleSpriteRect[i])))
          return true;
    }
    return false;
  }

  public boolean IsDyingOrDead() {
    return Dying || Dead;
  }

  public Rect getLogicalRect() {
    return new Rect(0,0,logicalWidth, logicalHeight);
  }

}

