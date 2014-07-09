package fester.Festaxian;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.*;

import static android.view.View.OnTouchListener;
import android.graphics.*;

/**
 * Created with IntelliJ IDEA.
 * User: RLester
 * Date: 21/05/12
 * Time: 21:35
 * To change this template use File | Settings | File Templates.
 */
public class GalaxianActivity extends Activity implements OnTouchListener{

  private GalaxianGameView v;
  private GalaxianGameEngine gameEngine;
  private FestaxianTitleScreen titleScreen;
  private boolean showTitleScreen = true;
  float x,y;
  boolean paused = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    v = new GalaxianGameView(this);
    Point screenDim = this.GetScreenDimensions();

    titleScreen = new FestaxianTitleScreen();
    titleScreen.Init(v.getContext(), screenDim.x, screenDim.y);

    // Make it full screen, remove banners and status strips
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
      WindowManager.LayoutParams.FLAG_FULLSCREEN);

    gameEngine = new GalaxianGameEngine();
    gameEngine.SetScreenDimensions(screenDim.x, screenDim.y);
    gameEngine.init(v.getContext());
    v.setOnTouchListener(this);
    x = y = 0;
    setContentView(v);
  }

  protected void onPause() {
    super.onPause();
    v.pause();

  }

  protected void onResume() {
    super.onResume();
    v.resume();
  }

  @Override
  public boolean onTouch(View view, MotionEvent me) {
   /*
    try {
      Thread.sleep(50);
    } catch(InterruptedException e) {
      e.printStackTrace();
    }
   */
    int mEvent = me.getAction();
    int which = -1;
    float x = 0;
    float y = 0;

    int id = me.getActionIndex();
    x = me.getX(id);
    y = me.getY(id);

    // if we're on the title screen
    // switch to the actual game
    if (showTitleScreen) {
      // Switch to the actual game engine display
      showTitleScreen = false;
      // Restart the background hum
      gameEngine.ResumeSound(2);
      // Trigger a new game
      gameEngine.PlaySound(1);
      gameEngine.StartNewGame();
    }
    return gameEngine.ProcessTouchEvent(mEvent, x, y);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    return gameEngine.KeyDown(keyCode);
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    Intent intent;
    // Display the setting screen when "menu" is pressed
    if (keyCode == KeyEvent.KEYCODE_MENU)        {
      intent = new Intent(this, FestaxianSettings.class);
      startActivity(intent);
    }
    // Show an OK/Cancel dialog box to quit
    // back to the title screen.
    if (keyCode == KeyEvent.KEYCODE_BACK && showTitleScreen==false) {
      // flag paused to prevent game update
      paused = true;
      AlertDialog.Builder ad = new AlertDialog.Builder(this) ;
      ad.setCancelable(false); // This blocks the 'BACK' button
      ad.setMessage("Quit Game?");
      ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          showTitleScreen = true;
          paused = false;
          gameEngine.ResetGame();
          // Pause the background hum
          gameEngine.PauseSound(2);
          dialog.dismiss();
        }
      });
      ad.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          paused = false;
          dialog.dismiss();
        }
      });

      ad.show();
      // Resume the game update engine
    }
    return gameEngine.KeyUp(keyCode);
  }

  private Point GetScreenDimensions() {
    DisplayMetrics metrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(metrics);
    Point sPoint = new Point();
    sPoint.y = metrics.heightPixels;
    sPoint.x = metrics.widthPixels;
    return sPoint;
  }

  public class GalaxianGameView extends SurfaceView implements Runnable {

    Thread gameThread = null;
    SurfaceHolder holder;
    boolean running = false;

    public GalaxianGameView(Context context) {
      super(context);
      holder = getHolder();
    }

    public void run() {
      while(running) {
        // perform canvas drawing
        if (!holder.getSurface().isValid()){
          continue;
        }

        if (!paused)  {
          Canvas c = holder.lockCanvas();
          c.drawARGB(255, 0, 0, 0);
          // Depending on the current context
          // draw the frame for either the title screen or
          // the actual game
          if (showTitleScreen)
            titleScreen.DrawTitleScreen(c, this.getContext());
          else
            gameEngine.DrawGameFrame(c);
          holder.unlockCanvasAndPost(c);
        }
      }
    }

    public void pause() {
      running = false;
      while(true) {
        try {
          gameThread.join();
        } catch(InterruptedException e) {
          e.printStackTrace();
        }
        break;
      }
      // Pause the background hum
      gameEngine.PauseSound(2);
      gameThread = null;
    }

    public void resume() {
      paused = false;
      running = true;
      // refresh the game engine (settings may have changed)
      Point screenDim = GetScreenDimensions();

      titleScreen.Init(v.getContext(), screenDim.x, screenDim.y);
      gameEngine.SetScreenDimensions(screenDim.x, screenDim.y);
      gameEngine.init(v.getContext());
      // Start a new game thread
      gameThread = new Thread(this);
      gameThread.start();
    }

  }
}
