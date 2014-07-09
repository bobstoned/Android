package fester.Festaxian;
import android.graphics.*;
/**
 * Created by RLester on 19/03/14.
 */
public class FlashingText {

  public FlashingText() {
    // flash delay of half a second
    timer.delay = 500;
    timer.Enabled = true;
  }

  private SpriteTimer timer = new SpriteTimer();

  private String currentText = "";

  public Paint TextStyle = new Paint() ;

  public void DrawText(String Text, Canvas canvas, int x, int y, double pixelSize, Typeface font, int color) {
    // Alternate between empty text and the specified text
    if (timer.Expired()) {
      if (currentText == ""){
        currentText = Text;
      }
      else {
        currentText = "";
      }
    }
    TextStyle.setColor(color);
    TextStyle.setStyle(Paint.Style.FILL);
    TextStyle.setTypeface(font);
    TextStyle.setTextSize((int)(pixelSize * 10));
    // If not empty draw the text
    if (currentText != "") {
      canvas.drawText(Text, x, y, TextStyle);
    }
  }
}
