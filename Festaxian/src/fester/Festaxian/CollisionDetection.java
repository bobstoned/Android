package fester.Festaxian;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;

/**
 * Created with IntelliJ IDEA.
 * User: rlester
 * Date: 21/04/12
 * Time: 20:17
 * To change this template use File | Settings | File Templates.
 */
public class CollisionDetection {

  public static boolean CompareSprites(Bitmap sprite1, Bitmap sprite2, Rect iRect1, Rect iRect2, int transColor, int scale) {
    Bitmap sprite1Scaled = GetScaledBitmap(sprite1, scale);
    Bitmap sprite2Scaled = GetScaledBitmap(sprite2, scale);
    int[] pixels1 = new int[iRect1.width() * iRect1.height()];
    int[] pixels2 = new int[iRect2.width() * iRect2.height()];
    boolean collision = false;
    // Get the pixels for the intersection Rect for both sprites
    // These will be used for comparison
    sprite1Scaled.getPixels(pixels1,0,iRect1.width(),iRect1.left,iRect1.top,iRect1.width(),iRect1.height());
    sprite2Scaled.getPixels(pixels2,0,iRect2.width(),iRect2.left,iRect2.top,iRect2.width(),iRect2.height());

    for(int i=0;i<=pixels1.length;i++)
      if (pixels1[i] != transColor && pixels2[i] != transColor)
        collision = true;
    return collision;
  }

  private static Bitmap GetScaledBitmap(Bitmap sprite, int scale) {
    Bitmap scaledBitmap = Bitmap.createBitmap(sprite.getWidth()*scale, sprite.getHeight()*scale, sprite.getConfig());
    Canvas canvas = new Canvas(scaledBitmap);
    canvas.drawBitmap(sprite, new Matrix(), null);
    return scaledBitmap;
  }


  // Get the intersection Rect of r1 and r2 local to r1
  public static Rect GetIntersectionRect(Rect i1, Rect r1, Rect r2) {
    boolean intersects = i1.setIntersect(r1, r2);
    Rect localRect;
    // Get the intersect rect local to the Rect (r1)
    localRect = new Rect(0,0,i1.left - r1.left, i1.top-r1.top);
    return localRect;
  }

}
