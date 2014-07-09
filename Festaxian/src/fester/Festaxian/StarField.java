package fester.Festaxian;

import android.graphics.*;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: rlester
 * Date: 26/04/12
 * Time: 14:52
 * To change this template use File | Settings | File Templates.
 */
public class StarField {

  private Star[] stars ;
  private int count;
  private Rect bounds;
  public Movement movement = new Movement() ;
  private double pixelSize = 2;

  public StarField(long moveDelay, double moveStep, double pixelSize){
    this.setPixelSize(pixelSize);
    this.movement.delay = moveDelay;
    this.movement.setMovementStep(moveStep);
  }

  public int xOffset = 0;

  public StarField(int count, Rect bounds){
    this.setCount(count);
    this.setBounds(bounds);
  }

  public Star[] Stars() {
    return stars;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
    if (bounds != null)
      CreateStars() ;
  }

  private void CreateStars()  {
    this.movement.Direction = Movement.MovementDirection.Down;
    this.movement.Style = Movement.MovementStyle.Wrap;

    stars = new Star[count];
    Random r = new Random();
    for(int i=0;i<count;i++)  {
      stars[i] = new Star();
      stars[i].Color = Color.argb(255, r.nextInt(255),r.nextInt(255), r.nextInt(255))  ;
      stars[i].Extent = bounds;
      stars[i].SpriteRect = new Rect(1,1,1,1);
      stars[i].Movement.delay = 2;
      stars[i].Movement.setMovementStep(2);
      stars[i].Movement.Direction = Movement.MovementDirection.Down;
      stars[i].Movement.Style = Movement.MovementStyle.Wrap;
      stars[i].Position = new Point(0,0);
      stars[i].Position.x = bounds.left + r.nextInt(bounds.width())-1;
      stars[i].Position.y = bounds.top + r.nextInt(bounds.height())-1;
    }
  }

  public void DrawStars(Canvas canvas) {
    for(int i=0; i<count;i++)
      DrawStar(stars[i], canvas) ;
  }

  public void MoveStars() {
    if (this.movement.CanMove())
      for(int i=0; i<count;i++)  {
        stars[i].Move(true);
      }
  }

  public void DrawStar(Star star, Canvas canvas) {
    Paint paint = new Paint();
    star.BlinkAnimation.AdvanceFrame();
    if (star.BlinkAnimation.FrameNo==0)
      paint.setColor(star.Color);
    else
      paint.setColor(Color.TRANSPARENT);
    switch((int)Math.round(pixelSize)) {
      case 1: canvas.drawPoint(star.Position.x, star.Position.y, paint);
        break;
      /*
      case 2: {
        canvas.drawLine(star.Position.x-1, star.Position.y-1,star.Position.x+1, star.Position.y-1, paint);
        canvas.drawLine(star.Position.x, star.Position.y-1,star.Position.x, star.Position.y+1, paint);
      }
      */
       default: {
        int starSize= (int)(pixelSize * 1.1) ;
        canvas.drawRect(star.Position.x, star.Position.y,star.Position.x+starSize, star.Position.y+starSize, paint);
       }
    }
  }


  public Rect getBounds() {
    return bounds;
  }

  public void setBounds(Rect bounds) {
    this.bounds = bounds;
    if (count != 0)
      CreateStars();
  }

  public double getPixelSize() {
    return pixelSize;
  }

  public void setPixelSize(double pixelSize) {
    this.pixelSize = pixelSize;
    this.movement.PixelSize = pixelSize;
  }
}
