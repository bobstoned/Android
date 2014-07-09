package fester.Festaxian;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;

/**
 * Created with IntelliJ IDEA.
 * User: rlester
 * Date: 18/04/12
 * Time: 23:34
 * To change this template use File | Settings | File Templates.
 */
public class SpriteSheet {

  public Point topLeft;
  public int spriteWidth ;
  public int spriteHeight ;
  public int gapX;
  public int gapY;

  public Rect GetSpriteRectAt(int col, int row) {
    int offSetx =  topLeft.x + (col * (spriteWidth + gapX) ) ;
    int offSety =  topLeft.y + (row * (spriteHeight + gapY) ) ;
    Rect sheetRect = new Rect(offSetx,offSety, offSetx+(spriteWidth), offSety+(spriteHeight));
    return sheetRect;
  }

  public void DrawSprite(Bitmap source, Canvas dest, Rect sourceRect, Point location, double scale)   {
    DrawSprite(source, dest, sourceRect, location, scale, 0, 0);
  }

  public void DrawSprite(Bitmap source, Canvas dest, Rect sourceRect, Point location, double scale, int width, int height)   {
    if (width==0) width = 1;
    if (height==0) height = 1;
    int x = location.x;
    int y = location.y;
    Rect destRect ;
    if (width==0 || height ==0) {
      // Use the source rectangle's dimensions
      width = sourceRect.width();
      height = sourceRect.height();
    }
    destRect = new Rect(x, y, x + ((int)(width * scale)), y + ((int)(height * scale))) ;
    dest.drawBitmap(source, sourceRect, destRect, null);
  }
}
