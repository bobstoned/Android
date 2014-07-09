package fester.Festaxian;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.example.R;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: RLester
 * Date: 23/05/12
 * Time: 12:13
 * To change this template use File | Settings | File Templates.
 */
public class GalaxianGameEngine {

  public SpriteSheet sheet = new SpriteSheet();
  private SpriteSheet playerSpriteSheet = new SpriteSheet();
  private SpriteSheet missileSheet = new SpriteSheet();
  private SpriteSheet scoreSheet = new SpriteSheet();
  public Bitmap sheetBitmap ;
  private Bitmap playerSheetBitmap;
  private Bitmap missileSheetBitmap;
  private Bitmap scoreSheetBitmap;
  private Bitmap buttonBitmap;
  private Bitmap livesLeftBitmap;
  private int spriteCount = 150;
  private Galaxian[] galaxians = new Galaxian[spriteCount];
  private GalaxianAttackGroup galaxianAttackGroup1;
  private GalaxianAttackGroup galaxianAttackGroup2;
  private Sprite playerShip = new Sprite();
  private Sprite playerMissile = new Sprite();
  private int playerPosMissileFire = 0;
  public Rect gameArea = new Rect(0,100,450,800);
  private Rect galArea = new Rect(0,150,450,400);
  private int screenWidth = 1024;
  private int screenHeight = 600;
  public double pixelSize = 0.5;
  private StarField starField ;
  private int score = 0;
  private int maxPlayerShips = 3;
  private int curPlayerShip = 1;
  private int curFleetNo = 1;
  private SpriteFormation formation = new SpriteFormation(gameArea, 10, 20, 2);
  // Horizontal Direction of player movement
  private int dx = 0;
  private int buttonWidth = 100;
  private Context context;
  private MediaPlayer mediaPlayer[] = new MediaPlayer[6];
  private boolean isDebugMode = false;
  private String graphicsStyle = "1";
  private boolean showControls = false;

  private Typeface retroFont ;
  private Paint fontPaint;
  private Paint playerNoPaint;

  private SpriteTimer waveIntervalTimer = new SpriteTimer();
  private SpriteTimer playerDeathPauseTimer = new SpriteTimer();
  private SpriteTimer newPlayerReadyTimer = new SpriteTimer();

  private FlashingText playerReadyText = new FlashingText();
  // Set to true at the start of a game
  public boolean GamesStart = true;
  public boolean GameOver = false;

  public void InitSounds() {
    mediaPlayer[0] = MediaPlayer.create(context, R.raw.galaxian_insert_coin) ;
    mediaPlayer[1] = MediaPlayer.create(context, R.raw.galaxian_background_hum) ;
    mediaPlayer[1].setLooping(true);
    mediaPlayer[2] = MediaPlayer.create(context, R.raw.galaxian_player_fire) ;
    mediaPlayer[3] = MediaPlayer.create(context, R.raw.galaxian_alien_base_death) ;
    mediaPlayer[4] = MediaPlayer.create(context, R.raw.galaxian_alien_death) ;
    mediaPlayer[5] = MediaPlayer.create(context, R.raw.galaxian_player_death) ;
  }

  public void PlaySound(int soundID) {
    mediaPlayer[soundID-1].seekTo(0);
    mediaPlayer[soundID-1].start();
  }

  public void PauseSound(int soundID) {
    mediaPlayer[soundID-1].pause();
  }

  public void ResumeSound(int soundID) {
    mediaPlayer[soundID-1].start();
  }

  public void init(Context context)
  {
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    graphicsStyle = sharedPref.getString("pref_graphics_style", "");
    showControls = sharedPref.getBoolean("pref_show_controls", true);
    isDebugMode = sharedPref.getBoolean("pref_debug_mode", false);

    this.context = context;

    retroFont = Typeface.createFromAsset(context.getAssets(), "Galaxian.ttf");

    fontPaint = new Paint();
    fontPaint.setColor(Color.RED);
    fontPaint.setStyle(Paint.Style.FILL);
    fontPaint.setTypeface(retroFont);
    fontPaint.setTextSize((int)(pixelSize * 10));

    playerNoPaint = new Paint();
    playerNoPaint.setColor(Color.WHITE);
    playerNoPaint.setStyle(Paint.Style.FILL);
    playerNoPaint.setTypeface(retroFont);
    playerNoPaint.setTextSize((int)(pixelSize * 10));

    // Speed settings for each type of object
    int starFieldDelay = 60;
    double starFieldMoveStep = 0.5;
    int playMissileDelay = 6;
    long galxAttackDelay = 30;
    double galxAttackStep = 1;
    double playMissileStep = pixelSize * 1.2;

    starField = new StarField(starFieldDelay, starFieldMoveStep, pixelSize);

    starField.setBounds(gameArea);
    starField.setPixelSize(pixelSize);
    starField.setCount(100);

    playerSpriteSheet.topLeft = new Point(0,0);
    playerSpriteSheet.gapX = 0;
    playerSpriteSheet.gapY = 0;

    formation.pixelSize = pixelSize;

    missileSheet.topLeft = new Point(0,0);
    missileSheet.gapX = 0;
    missileSheet.gapY = 0;
    missileSheet.spriteHeight = 2;
    missileSheet.spriteWidth = 1;

    scoreSheet.topLeft = new Point(0,0);
    scoreSheet.gapX =0;
    scoreSheet.gapY =0;
    scoreSheet.spriteHeight = 12;
    scoreSheet.spriteWidth = 12;

    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inScaled = false;
    options.inDensity = 100;


    if (graphicsStyle.equals("1"))  {
      // Define the sprite sheet for the classic galaxian graphics
      sheet.gapX = 8;
      sheet.gapY = 8;
      sheet.topLeft = new Point(111,0);
      sheet.spriteHeight = 12;
      sheet.spriteWidth = 12;
      sheetBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.galaxian_sheet, options)  ;

      playerSpriteSheet.spriteHeight = 32;
      playerSpriteSheet.spriteWidth = 32;
      playerSheetBitmap =  BitmapFactory.decodeResource(context.getResources(), R.drawable.playership, options)  ;
    }
    else {
      // Define the sprite sheet for the modern galaxian graphics
      sheet.gapX = 0;
      sheet.gapY = 0;
      sheet.topLeft = new Point(0,0);
      sheet.spriteHeight = 80;
      sheet.spriteWidth = 80;
      sheetBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.galaxian_sheet_modern, options) ;

      playerSpriteSheet.spriteHeight = 150;
      playerSpriteSheet.spriteWidth = 150;
      playerSheetBitmap =  BitmapFactory.decodeResource(context.getResources(), R.drawable.playership_modern, options)  ;
    }

    missileSheetBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.missile_sheet, options);
    scoreSheetBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.score_sheet, options);
    buttonBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.arcade_button, options);
    livesLeftBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.livesleft, options);
    // Define user ship sprite
    CreatePlayerShip();
    CreateGalaxians(galxAttackDelay, galxAttackStep);
    CreateMissile(playMissileDelay, playMissileStep);
    InitSounds();
    curFleetNo= 1;
  }

  public void StartNewGame() {
    GamesStart = true;
    GameOver = false;
    curPlayerShip = 1;
    newPlayerReadyTimer.delay = 3000;
    newPlayerReadyTimer.Enabled = true;
    newPlayerReadyTimer.Reset();
  }

  // Create the two sets of attack groups
  // Consisting of one base ship(yellow) and two wing men (red)
  private void CreateGalaxianAttackGroups() {
    galaxianAttackGroup1 = new GalaxianAttackGroup();
    galaxianAttackGroup2 = new GalaxianAttackGroup();
  }

  // The game area is dictated by the physical device screen
  // dimensions. Obviously there are a set of very common
  // screen resolutions. These are treated as special cases
  // with very specific settings.
  public void SetScreenDimensions(int width, int height) {
    boolean specificSize = false;
    screenWidth = width;
    screenHeight = height;
    galArea.right = width;
    galArea.bottom = height;
    gameArea = new Rect();
    gameArea.left = 0;
    gameArea.top = 0;
    gameArea.right = width;
    gameArea.bottom = height-10;

    if (height == 320) {
      specificSize = true;
      formation.gap = 2;
      pixelSize = 0.5;
      buttonWidth = 40;
    }
    // Nexus 10 - Pixel overload!
    if (height == 1504 && width == 2560) {
      specificSize = true;
      formation.gap = 3;
      pixelSize = 5;
      buttonWidth = 300;
      gameArea.left = 700;
      gameArea.top = 0;
      gameArea.right = 1960;
      gameArea.bottom = 1350;
      galArea.left = gameArea.left;
      galArea.top = 200;
      galArea.right = gameArea.right;
      galArea.bottom = gameArea.bottom;
    }
    // Nexus 10 - Pixel overload!
    if (height > 2400 && width > 1500) {
      specificSize = true;
      formation.gap = 4;
      //pixelSize = 2.3;
      pixelSize = 5;
      buttonWidth = 300;
      gameArea.left = 0;
      gameArea.top = 0;
      gameArea.right = width;
      gameArea.bottom = 1500;
      galArea.left = gameArea.left;
      galArea.top = 100;
      galArea.right = gameArea.right;
      galArea.bottom = gameArea.bottom;
    }
    // HTC DEsire etc (800 x 480)
    if ( (height >= 700 && height<= 800) && width == 480) {
      specificSize = true;
      formation.gap = 1;
      pixelSize = 2.2;
      //pixelSize = 4;
      buttonWidth = 100;
      galArea.left = 0;
      galArea.right = 480;
      galArea.bottom = 550;
      galArea.top = 50;
      gameArea.left = 0;
      gameArea.top = 0;
      gameArea.right = 480;
      gameArea.bottom = 550;
    }
    if (height == 480 && (width >= 700 && width<= 800)) {
      specificSize = true;
      formation.gap = 2;
      pixelSize = 1.8;
      buttonWidth = 80;
      galArea.left = 200;
      galArea.right = 600;
      galArea.bottom = 400;
      galArea.top = 0;
      gameArea.left = 200;
      gameArea.top = 0;
      gameArea.right = 600;
      gameArea.bottom = height;
    }

    //ZTE Grand X v970m ( 960 x 540)
    if ( (height >= 900 && height<= 960) && width == 540) {
      specificSize = true;
      formation.gap = 1;
      pixelSize = 2.6;
      buttonWidth = 100;
      galArea.left = 0;
      galArea.right = 540;
      galArea.bottom = 710;
      galArea.top = 50;
      gameArea.left = 0;
      gameArea.top = 0;
      gameArea.right = 540;
      gameArea.bottom = 710;
    }
    if (height == 540 && (width >= 900 && width<= 960)) {
      specificSize = true;
      formation.gap = 2;
      pixelSize = 2.2;
      buttonWidth = 80;
      galArea.left = 280;
      galArea.right = 680;
      galArea.bottom = 460;
      galArea.top = 0;
      gameArea.left = 280;
      gameArea.top = 0;
      gameArea.right = 680;
      gameArea.bottom = height;
    }

    if (!specificSize)  {
      if (height > width) {
        if (width >= 700 && width < 1025)  {
          formation.gap = 12;
          pixelSize = 3;
          buttonWidth = 100;
        }
        if (width > 320 && width < 700)  {
          formation.gap = 8;
          pixelSize = 2;
          buttonWidth = 60;
        }
      }
      else {
        if (height >= 700 && height < 1025)  {
          formation.gap = 12;
          pixelSize = 3;
          buttonWidth = 100;
        }
        if (height > 320 && height < 700)  {
          formation.gap = 8;
          pixelSize = 2;
          buttonWidth = 60;
        }
      }
    }
    formation.Bounds = galArea;
  }

  private void CreatePlayerShip() {
    playerShip.setPixelSize(pixelSize);
    playerShip.logicalWidth = 32;
    playerShip.logicalHeight = 32;
    playerShip.Extent =  new Rect(gameArea.left,gameArea.bottom-220,gameArea.right,gameArea.bottom);
    playerShip.SpriteRect = playerSpriteSheet.GetSpriteRectAt(0, 0);

    playerShip.MultipleSpriteRect = new Rect[4];
    playerShip.MultipleSpriteRect[0] = new Rect(10,17,20,22);
    playerShip.MultipleSpriteRect[1] = new Rect(14,14,16,16);
    playerShip.MultipleSpriteRect[2] = new Rect(12,10,18,13);
    playerShip.MultipleSpriteRect[3] = new Rect(15,5,15,8);

    playerShip.Movement.Direction = Movement.MovementDirection.Right;
    playerShip.Movement.Style = Movement.MovementStyle.Limit;
    playerShip.Movement.delay = 5;
    playerShip.Movement.setMovementStep(0.5 * pixelSize);
    playerShip.Animation.Frames = new Point[1] ;
    playerShip.Animation.Frames[0] = new Point(0,0);
    playerShip.Position = new Point(gameArea.left,gameArea.bottom-( (int)(playerShip.logicalHeight * pixelSize) ) );
    playerShip.DeathAnimation.FrameDelay = 200;
    playerShip.DeathAnimation.Loop = false;
    playerShip.DeathAnimation.Frames = new Point[4];
    playerShip.DeathAnimation.Frames[0] = new Point(1,0);
    playerShip.DeathAnimation.Frames[1] = new Point(2,0);
    playerShip.DeathAnimation.Frames[2] = new Point(1,1);
    playerShip.DeathAnimation.Frames[3] = new Point(2,1);
  }

  private void CreateGalaxians(long attackDelay, double attackStep) {
    Random ran = new Random();
    int row = -1;
    int col = 0;
    int i = 0;
    CreateGalaxianAttackGroups();
    // Define Galaxian sprites
    i =0;
    row = 0;
    col = 3;
    galaxians[i] = CreateGalaxian(3, attackDelay, attackStep);
    galaxians[i].Player = playerShip;
    formation.getMatrix()[col][row] = galaxians[i];

    i++;
    col = 6;
    galaxians[i] = CreateGalaxian(3, attackDelay, attackStep);
    formation.getMatrix()[col][row] = galaxians[i];
    i++;


    row = 1;
    for(int c=2; c<=7; c++) {
      galaxians[i] = CreateGalaxian(1, attackDelay, attackStep);
      formation.getMatrix()[c][row] = galaxians[i];
      i++;
    }

    row = 2;
    for(int c=1; c<=8; c++) {
      galaxians[i] = CreateGalaxian(2, attackDelay, attackStep);
      formation.getMatrix()[c][row] = galaxians[i];
      i++;
    }

    for (int r=3;r<=5;r++)
      for(int c=0; c<=9; c++) {
        galaxians[i] = CreateGalaxian(0, attackDelay, attackStep);
        formation.getMatrix()[c][r] = galaxians[i];
        i++;
      }

    spriteCount = i;
    SetupAttackGroups();
    formation.EnableAttack() ;
    // Create the initial positions of the galaxians in the formation
    // scale to a virtual dimension of 16x16 pixels (scaled to screen size)
    formation.PositionSprites(12, 12);
    formation.movement.delay = 70;
  }

  private void SetupAttackGroups() {
    galaxianAttackGroup1.AssignAttackShips(formation.getMatrix()[3][0], formation.getMatrix()[2][1], formation.getMatrix()[3][1]);
    galaxianAttackGroup2.AssignAttackShips(formation.getMatrix()[6][0], formation.getMatrix()[6][1], formation.getMatrix()[7][1]);

    galaxianAttackGroup1.coreShip.AttackTimer.Enabled = true;
    galaxianAttackGroup1.coreShip.AttackTimer.Expired();

    galaxianAttackGroup2.coreShip.AttackTimer.Enabled = true;
    galaxianAttackGroup2.coreShip.AttackTimer.Expired();
  }

  public Galaxian CreateGalaxian(int shipType, long attackDelay, double attackStep) {
    Random r = new Random();
    int uppershipType;
    int lowershipType;

    Galaxian galaxian = new Galaxian();
    galaxian.Player = playerShip;

    lowershipType = (shipType * 2);
    uppershipType = (shipType * 2)+1;

    switch(shipType) {
      case 0 : galaxian.setShipType(Galaxian.ShipType.Drone);
        break;
      case 1 : galaxian.setShipType(Galaxian.ShipType.BodyGuard);
        break;
      case 2 : galaxian.setShipType(Galaxian.ShipType.Raider);
        break;
      case 3 : galaxian.setShipType(Galaxian.ShipType.Base);
        break;
    }

    galaxian.setPixelSize(pixelSize);
    galaxian.Position = new Point(r.nextInt(340), r.nextInt(360));
    galaxian.SpriteRect = sheet.GetSpriteRectAt(r.nextInt(13),r.nextInt(5));
    // Set the logical width and height of the galaxian before scaling for resolution
    // this is based on the original bitmap dimensions
    galaxian.logicalHeight = 16;
    galaxian.logicalWidth = 16;
    galaxian.Extent = gameArea;
    galaxian.Movement.delay = 0;
    galaxian.Movement.setMovementStep(attackStep);
    galaxian.attackMovement.delay = attackDelay;
    galaxian.Movement.setMovementPixelSize((int)pixelSize);
    galaxian.attackMovement.setMovementPixelSize((int)(3*pixelSize));
    galaxian.dockingMovement.setMovementPixelSize((int)(3*pixelSize));
    galaxian.Movement.Style = Movement.MovementStyle.Cyclic;

    galaxian.Animation.FrameDelay = 200;
    galaxian.Animation.Frames = new Point[3];

    if (shipType <= 2)  {
      galaxian.Animation.Frames[0] = new Point(8,lowershipType);
      galaxian.Animation.Frames[1] = new Point(0,uppershipType);
      galaxian.Animation.Frames[2] = new Point(1,uppershipType);
    }
    else {
      galaxian.Animation.Frames[0] = new Point(9,0);
      galaxian.Animation.Frames[1] = new Point(10,0);
      galaxian.Animation.Frames[2] = new Point(10,0);
    }

    galaxian.DeathAnimation.FrameDelay = 100;
    galaxian.DeathAnimation.Loop = false;
    galaxian.DeathAnimation.Frames = new Point[4];
    galaxian.DeathAnimation.Frames[0] = new Point(10,5);
    galaxian.DeathAnimation.Frames[1] = new Point(11,5);
    galaxian.DeathAnimation.Frames[2] = new Point(12,5);
    galaxian.DeathAnimation.Frames[3] = new Point(13,5);

    if (shipType <= 2)
      SetStandardGalaxianAnimations(galaxian, lowershipType) ;
    else
      SetBaseShipGalaxianAnimations(galaxian) ;

    galaxian.missileFrame = new Point(1,0) ;
    galaxian.missileRect = missileSheet.GetSpriteRectAt(1,0);

    return galaxian;
  }

  private void SetStandardGalaxianAnimations(Galaxian galaxian, int lowershipType){
    int uppershipType = lowershipType+1;
    galaxian.AttackAnimation.Frames = new Point[9];
    galaxian.AttackAnimation.Frames[0] = new Point(0, lowershipType);  //90
    galaxian.AttackAnimation.Frames[1] = new Point(8, uppershipType);  //120
    galaxian.AttackAnimation.Frames[2] = new Point(7, uppershipType);  //140
    galaxian.AttackAnimation.Frames[3] = new Point(6, uppershipType);  //160
    galaxian.AttackAnimation.Frames[4] = new Point(5, uppershipType);  //180

    galaxian.AttackAnimation.Frames[5] = new Point(1, lowershipType);  //60
    galaxian.AttackAnimation.Frames[6] = new Point(2, lowershipType);  //40
    galaxian.AttackAnimation.Frames[7] = new Point(3, lowershipType);  //30
    galaxian.AttackAnimation.Frames[8] = new Point(4, lowershipType);  //0

    galaxian.getDiveAnimation().FrameDelay = 100;
    galaxian.getDiveAnimation().Loop = false;
    galaxian.getDiveAnimation().Frames = new Point[9];
    galaxian.getDiveAnimation().Frames[0] = new Point(1,uppershipType);
    galaxian.getDiveAnimation().Frames[1] = new Point(2,uppershipType);
    galaxian.getDiveAnimation().Frames[2] = new Point(3,uppershipType);
    galaxian.getDiveAnimation().Frames[3] = new Point(4,uppershipType);
    galaxian.getDiveAnimation().Frames[4] = new Point(5,uppershipType);
    galaxian.getDiveAnimation().Frames[5] = new Point(6,uppershipType);
    galaxian.getDiveAnimation().Frames[6] = new Point(7,uppershipType);
    galaxian.getDiveAnimation().Frames[7] = new Point(8,uppershipType);
    galaxian.getDiveAnimation().Frames[8] = new Point(0,lowershipType);

    galaxian.getDockingAnimation().FrameDelay = 200;
    galaxian.getDockingAnimation().Loop = false;
    galaxian.getDockingAnimation().Frames = new Point[9];
    galaxian.getDockingAnimation().Frames[0] = new Point(8,uppershipType);
    galaxian.getDockingAnimation().Frames[1] = new Point(7,uppershipType);
    galaxian.getDockingAnimation().Frames[2] = new Point(6,uppershipType);
    galaxian.getDockingAnimation().Frames[3] = new Point(5,uppershipType);
    galaxian.getDockingAnimation().Frames[4] = new Point(4,uppershipType);
    galaxian.getDockingAnimation().Frames[5] = new Point(3,uppershipType);
    galaxian.getDockingAnimation().Frames[6] = new Point(2,uppershipType);
    galaxian.getDockingAnimation().Frames[7] = new Point(1,uppershipType);
    galaxian.getDockingAnimation().Frames[8] = new Point(8,lowershipType);
  }

  private void SetBaseShipGalaxianAnimations(Galaxian galaxian){
    galaxian.AttackAnimation.Frames = new Point[9];
    galaxian.AttackAnimation.Frames[0] = new Point(12, 2);  //90
    galaxian.AttackAnimation.Frames[1] = new Point(10, 2);  //120
    galaxian.AttackAnimation.Frames[2] = new Point(13, 1);  //140
    galaxian.AttackAnimation.Frames[3] = new Point(12, 1);  //160
    galaxian.AttackAnimation.Frames[4] = new Point(11, 1);  //180

    galaxian.AttackAnimation.Frames[5] = new Point(13, 2);  //60
    galaxian.AttackAnimation.Frames[6] = new Point(9, 3);  //40
    galaxian.AttackAnimation.Frames[7] = new Point(11, 3);  //30
    galaxian.AttackAnimation.Frames[8] = new Point(13, 3);  //0

    galaxian.getDiveAnimation().FrameDelay = 100;
    galaxian.getDiveAnimation().Loop = false;
    galaxian.getDiveAnimation().Frames = new Point[12];
    galaxian.getDiveAnimation().Frames[0] = new Point(11,0);
    galaxian.getDiveAnimation().Frames[1] = new Point(12,0);
    galaxian.getDiveAnimation().Frames[2] = new Point(13,0);
    galaxian.getDiveAnimation().Frames[3] = new Point(9,1);
    galaxian.getDiveAnimation().Frames[4] = new Point(10,1);
    galaxian.getDiveAnimation().Frames[5] = new Point(11,1);
    galaxian.getDiveAnimation().Frames[6] = new Point(12,1);
    galaxian.getDiveAnimation().Frames[7] = new Point(13,1);
    galaxian.getDiveAnimation().Frames[8] = new Point(9,2);
    galaxian.getDiveAnimation().Frames[9] = new Point(10,2);
    galaxian.getDiveAnimation().Frames[10] = new Point(11,2);
    galaxian.getDiveAnimation().Frames[11] = new Point(12,2);

    galaxian.getDockingAnimation().FrameDelay = 200;
    galaxian.getDockingAnimation().Loop = false;
    galaxian.getDockingAnimation().Frames = new Point[13];
    galaxian.getDockingAnimation().Frames[0] = new Point(12,2);
    galaxian.getDockingAnimation().Frames[1] = new Point(13,2);
    galaxian.getDockingAnimation().Frames[2] = new Point(9,3);
    galaxian.getDockingAnimation().Frames[3] = new Point(10,3);
    galaxian.getDockingAnimation().Frames[4] = new Point(11,3);
    galaxian.getDockingAnimation().Frames[5] = new Point(12,3);
    galaxian.getDockingAnimation().Frames[6] = new Point(13,3);
    galaxian.getDockingAnimation().Frames[7] = new Point(9,4);
    galaxian.getDockingAnimation().Frames[8] = new Point(10,4);
    galaxian.getDockingAnimation().Frames[9] = new Point(11,4);
    galaxian.getDockingAnimation().Frames[10] = new Point(12,4);
    galaxian.getDockingAnimation().Frames[11] = new Point(13,4);
    galaxian.getDockingAnimation().Frames[12] = new Point(10,0);
  }

  public void DrawGameFrame(Canvas canvas) {
    // Draw buttons if turned on
    if (showControls)
      DrawControlButtons(canvas);
    // Move the player ship in accordance with the any directional button press (dx)
    MovePlayer(canvas, dx);
    // Move the player ship missile if it hasn't collided or completed its movement
    if (!playerMissile.Dying && !playerMissile.Dead)
      MoveMissile(canvas);
    // Display the score at the top of the screen
    DisplayScore(canvas);

    Rect fRect = null;
    starField.DrawStars(canvas);
    starField.MoveStars();

    formation.SetFormationDirection(gameArea);

    if (!GamesStart) {
      UpdateGalaxians(canvas);
      // If the wave is ready draw the ships
      DrawGalaxians(canvas);
    }
    if (newPlayerReadyTimer.Enabled) {
      // Show a message "Ready player x"
      DisplayReadyMessage(canvas);
    }

    if (newPlayerReadyTimer.Expired() || !newPlayerReadyTimer.Enabled || GameOver) {
      GamesStart = false;
      newPlayerReadyTimer.Enabled = false;
    }

    boolean allDead = true;
    for(int i=0;i<spriteCount;i++) {
      if (!galaxians[i].Dead)
        allDead = false;
    }

    // Start again if all dead
    if (allDead) {
      if (!waveIntervalTimer.Enabled) {
        // wait 4 seconds until displaying the next wave
        waveIntervalTimer.delay = 4000;
        waveIntervalTimer.Reset();
        // start the interval timer
        waveIntervalTimer.Enabled = true;
      }
      // Reset formation position for new wave
      if (waveIntervalTimer.Expired()) {
        waveIntervalTimer.Enabled = false;
        ResetGame();
      }
    }
    if (curPlayerShip > maxPlayerShips) {
      GameOver = true;
      DisplayGameOverMessage(canvas);
    }
    try {
      // When the player dies, wait until all enemies have returned to
      // their formation positions before re-enabling the attack.
      // If this is shorter than the death pause, wait for that timer to expire
      // This prevents more bullets instantly killing the player again
      // If its the last player ship game over is displayed
      if (formation.AllDocked() && playerShip.Dead && playerDeathPauseTimer.Expired() && !GameOver) {
        playerDeathPauseTimer.Enabled = false;
        playerShip.Dying = false;
        playerShip.Dead = false;
        playerShip.DeathAnimation.Reset();
        // Re-start the formation attack
        formation.EnableAttack() ;
      }
    }
    catch(Exception ex) {
      System.out.println("Error in reset player");
    }
  }

  private void UpdateGalaxians(Canvas canvas) {
    // Update the Galaxian attack groups
    // Only one can attack at a time.
    if (galaxianAttackGroup2.IsAttacking() )
      galaxianAttackGroup1.Update(false);
    else
      galaxianAttackGroup1.Update(true);

    if (galaxianAttackGroup1.IsAttacking() )
      galaxianAttackGroup2.Update(false);
    else
      galaxianAttackGroup2.Update(true);

    // Display the score for the base ship if applicable
    DrawAttackGroupBaseShipScore(galaxianAttackGroup1, canvas) ;
    DrawAttackGroupBaseShipScore(galaxianAttackGroup2, canvas) ;

    // Move the whole formation as one
    formation.Move();
  }

  private void DrawGalaxians(Canvas canvas) {
    int boxColor;
    for(int i=0;i<spriteCount;i++)  {
      boxColor = Color.WHITE;

      // Move all non formation Galaxians if not in an attack group
      // Update any logic independent state
      if (!IsAttackGroupShip(galaxians[i])) {
        galaxians[i].Move(false);
        galaxians[i].Update();
      }
      DrawSprite(galaxians[i], canvas);
      // Draw the Galaxian on the canvas
      if (!galaxians[i].Dead) {
        // Is the player missile colliding with the galaxian sprite
        if (!playerMissile.Dying && !playerMissile.Dead && !galaxians[i].Dying && !galaxians[i].Dead)
          if ( galaxians[i].IsCollision (playerMissile.GetBoundingBox() )) {
            // Increase the players score
            score = score + galaxians[i].getScore();
            galaxians[i].Dying = true;
            playerMissile.Dying = true;
            if (galaxians[i].getShipType() != Galaxian.ShipType.Base)  {
              PlaySound(5);
            }
            else {
              // Need to find out which attack group this base ship is part of
              // then set the points based on the wingmen status
              if (galaxianAttackGroup1.IsAttackGroupShip(galaxians[i]))
                galaxianAttackGroup1.SetDeathAnimationPoints();
              if (galaxianAttackGroup2.IsAttackGroupShip(galaxians[i]))
                galaxianAttackGroup2.SetDeathAnimationPoints();
              if (galaxians[i].getScore() == 800)
                // Both wingmen killed first so max points (yay!), play "whirly" sound!
                PlaySound(4);
              else
                // normal kill sound effect
                PlaySound(5);
            }
            boxColor = Color.RED;
          }
      }
      // Check for collisions
      if (!playerShip.Dying && !playerShip.Dead) {
        // Check for collisions between the player ship and enemy missiles
        for(int m=0;m<=galaxians[i].missileCount-1;m++)
          if (!galaxians[i].missiles[m].Dead && playerShip.IsCollision(galaxians[i].missiles[m].GetBoundingBox()))  {
            InitiatePlayerDeath(galaxians[i].missiles[m]);
          }
        // Check for collisions between the player ship and enemy ships
        if(!playerShip.Dying && !playerShip.Dead)
          if (!galaxians[i].Dead && playerShip.IsCollision(galaxians[i].GetBoundingBox()))  {
            InitiatePlayerDeath(galaxians[i]);
          }
      }
    }
  }

  private void DisplayReadyMessage(Canvas canvas) {
    int x = galArea.left + (int)(galArea.width() / 2) - (int)(pixelSize * (8 * 7));
    int y = galArea.bottom - (int)(galArea.height() /2);
    playerReadyText.DrawText("Ready Player 1", canvas, x, y, pixelSize, retroFont, Color.BLUE);
  }

  private void DisplayGameOverMessage(Canvas canvas) {
    int x = galArea.left + (int)(galArea.width() / 2) - (int)(pixelSize * (8 * 7));
    int y = galArea.bottom - (int)(galArea.height() /2);
    playerReadyText.DrawText("Game Over", canvas, x, y, pixelSize, retroFont, Color.RED);
  }

  private void InitiatePlayerDeath(Sprite enemyObject) {
    playerShip.Dying = true;
    // Player death sound
    PlaySound(6);
    enemyObject.Dead = true;
    if (curPlayerShip <= maxPlayerShips)
      curPlayerShip++;
    playerDeathPauseTimer.delay = 4000;
    playerDeathPauseTimer.Reset();
    playerDeathPauseTimer.Enabled = true;
    newPlayerReadyTimer.Reset();
    newPlayerReadyTimer.Enabled = true;
  }

  // Draws the control buttons on the screen (left, right and fire)
  private void DrawControlButtons(Canvas canvas) {
    int margin = (int)(buttonWidth/2);
    Rect butRect = new Rect(0,0,64,64);
    Rect butDestRect = new Rect(0,screenHeight - buttonWidth - margin, buttonWidth,screenHeight - margin);
    Rect butDest2Rect = new Rect(buttonWidth + 20,screenHeight-buttonWidth - margin, (buttonWidth * 2) + 20,screenHeight - margin);
    Rect butDest3Rect = new Rect(screenWidth - buttonWidth,(screenHeight - buttonWidth) - margin, screenWidth,screenHeight - margin);

    canvas.drawBitmap(buttonBitmap, butRect, butDestRect, null);
    canvas.drawBitmap(buttonBitmap, butRect, butDest2Rect, null);
    canvas.drawBitmap(buttonBitmap, butRect, butDest3Rect, null);
  }

  public void ResetGame() {
    // Reset formation position for new wave
    formation.PositionSprites(16,16);
    SetupAttackGroups();
    for(int i=0;i<spriteCount;i++)  {
      galaxians[i].Dying = false;
      galaxians[i].Dead = false;
      galaxians[i].DeathAnimation.Reset();
    }
    // Increment the current fleet count
    curFleetNo++;
  }

  private boolean IsAttackGroupShip(Galaxian ship) {
    if (galaxianAttackGroup1.IsAttackGroupShip(ship))
      return true;
    if (galaxianAttackGroup2.IsAttackGroupShip(ship))
      return true;
    return false;
  }

  private void DisplayScore(Canvas canvas) {
    canvas.drawText(Integer.toString(score), 10, (int)(pixelSize*12), fontPaint);
    canvas.drawText("1UP", (int)(pixelSize*80), (int)(pixelSize*12), playerNoPaint);

    if (curFleetNo < 10)
      for(int i=0;i<curFleetNo;i++) {
        scoreSheet.DrawSprite(scoreSheetBitmap, canvas, scoreSheet.GetSpriteRectAt(0,0), new Point( (int)(pixelSize*120) +(int)(i*8*pixelSize),(int)(pixelSize*2)), pixelSize, 12, 12);
      }
    else {
      int dec = curFleetNo / 10 ;
      int rem = curFleetNo % 10;
      for(int i=0;i<dec;i++) {
        scoreSheet.DrawSprite(scoreSheetBitmap, canvas, scoreSheet.GetSpriteRectAt(1,0), new Point((int)(pixelSize*120) +(int)(i*12*pixelSize),(int)(pixelSize*2)), pixelSize, 12, 12);
      }
      for(int i=0;i<rem;i++) {
        scoreSheet.DrawSprite(scoreSheetBitmap, canvas, scoreSheet.GetSpriteRectAt(0,0), new Point((int)(pixelSize*120) +(int)(i*8*pixelSize) + (int)(dec*12*pixelSize),(int)(pixelSize*2)), pixelSize, 12, 12);
      }
    }

    for (int i=0;i<maxPlayerShips-curPlayerShip;i++) {
      Rect sourceRect = new Rect(0, 0, 16, 16);
      int offsetStart = galArea.left + ((i+1) * ((int)(12 * pixelSize)));
      int offsetEnd =  galArea.left + ((i+2) * ((int)(12 * pixelSize)));
      Rect destRect = new Rect(offsetStart, galArea.bottom+16, offsetEnd, galArea.bottom+16 + ((int)(12 * pixelSize))) ;
      canvas.drawBitmap(livesLeftBitmap, sourceRect, destRect, null);
    }
  }

  public boolean KeyDown(int key){
    if (key == KeyEvent.KEYCODE_DPAD_LEFT )
      dx = -1;
    if (key == KeyEvent.KEYCODE_DPAD_RIGHT)
      dx = +1;
    if (key == KeyEvent.KEYCODE_DPAD_CENTER)
      FirePlayerMissile();
    return true;
  }

  public boolean KeyUp(int key){
    // If released a directional key stop moving the player ship
    if (key == KeyEvent.KEYCODE_DPAD_LEFT || key == KeyEvent.KEYCODE_DPAD_RIGHT)
      dx = 0;
    return true;
  }

  public boolean ProcessTouchEvent(int event, float x, float y) {
    if (event == MotionEvent.ACTION_POINTER_2_DOWN)  {
      dx = dx;
    }
    if (x > 0 && x <= 0 + buttonWidth)
      dx = -1;
    if ( (x > buttonWidth + 20)  && (x <= (buttonWidth * 2) + 20) )
      dx = +1;
    if (x > screenWidth-buttonWidth  && x <= screenWidth)
      FirePlayerMissile();

    if (event == MotionEvent.ACTION_UP)
      dx = 0;
    return true;
   }

  private void FirePlayerMissile() {
    if ( (playerMissile.Movement.Complete ||  playerMissile.Dead ||  playerMissile.Dying) && !playerShip.IsDyingOrDead()) {
      playerMissile.Dead =false;
      playerMissile.Dying = false;
      playerMissile.Movement.Complete = false;
      playerMissile.Position.x = playerShip.Position.x + (int)(pixelSize * 14) ;
      playerMissile.Position.y = playerShip.Position.y;
      PlaySound(3);
    }
  }

  private void DrawSprite(Galaxian sprite, Canvas canvas) {
    if (sprite.Position == null)
      sprite.Position = new Point(20,20);

    if (!sprite.Drawing && !sprite.Dead) {
      Point frame;
      frame = sprite.GetFrame();
      sprite.SpriteRect = sheet.GetSpriteRectAt(frame.x, frame.y);
      // Draw the sprite using a sprite(Rect) from the sprite sheet

      if (!isDebugMode)
        sheet.DrawSprite(sheetBitmap, canvas, sprite.SpriteRect, sprite.Position, pixelSize, 12, 12);
      else {
        // Draw a box representing the bounds of the sprite
        if (sprite.getStatus() != Galaxian.AttackStatus.Formation)
          sprite.DrawFormationBoundingBox(canvas, Color.WHITE);
        sprite.DrawBoundingBox(canvas, Color.RED);
      }
      if (!sprite.Dying) {
        //if (formation.movement.CanMove())
        //sprite.Move(true);
      }

    }
    // Draw missiles
    for(int m=0; m<sprite.missileCount; m++)
      if (!sprite.missiles[m].Dead)
        missileSheet.DrawSprite(missileSheetBitmap, canvas,  sprite.missiles[m].SpriteRect, sprite.missiles[m].Position, pixelSize, 1, 3);

  }

  private void MovePlayer(Canvas canvas, int dx){
    if (!playerShip.Drawing && !playerShip.Dead) {
      playerShip.AdvanceFrame() ;
      if (dx < 0) {
        playerShip.Movement.Direction = Movement.MovementDirection.Left;
        playerShip.Move(false);
      }
      else if (dx >0) {
        playerShip.Movement.Direction = Movement.MovementDirection.Right;
        playerShip.Move(false);
      }
      Point frame;
      frame = playerShip.GetFrame();
      playerShip.SpriteRect = playerSpriteSheet.GetSpriteRectAt(frame.x, frame.y);
      // Draw the sprite using a sprite(Rect) from the sprite sheet
      if (!isDebugMode) {
        sheet.DrawSprite(playerSheetBitmap, canvas, playerShip.SpriteRect, playerShip.Position, pixelSize, 32, 32);
      }
      else {
        playerShip.DrawCollisionBoxes(canvas, Color.YELLOW);
      }
      //playerShip.DrawCollisionBoxes(canvas, Color.RED);
    }
  }

  private void MoveMissile(Canvas canvas) {
    if (!playerMissile.Drawing && !playerMissile.Dead && !playerMissile.Movement.Complete ) {
      Point frame;
      frame = playerMissile.GetFrame();
      playerMissile.SpriteRect = missileSheet.GetSpriteRectAt(frame.x, frame.y);
      // Draw the sprite using a sprite(Rect) from the sprite sheet
      missileSheet.DrawSprite(missileSheetBitmap, canvas, playerMissile.SpriteRect, playerMissile.Position, pixelSize, 1, 3);
      // Draw a box representing the bounds of the sprite
      //playerMissile.DrawBoundingBox(canvas, 2, Color.WHITE);
      if (!playerMissile.Dying && !playerMissile.Dead) {
        playerMissile.Move(false);
      }

    }
  }

  private void DrawAttackGroupBaseShipScore(GalaxianAttackGroup group,  Canvas canvas) {
    if (group.BaseShip.Dead && group.BaseShipDeathScoreSprite.Dying) {
      Point frame;
      frame = group.BaseShipDeathScoreSprite.GetFrame();
      group.BaseShipDeathScoreSprite.SpriteRect = scoreSheet.GetSpriteRectAt(frame.x, frame.y);
      scoreSheet.DrawSprite(scoreSheetBitmap, canvas, group.BaseShipDeathScoreSprite.SpriteRect, group.BaseShipDeathScoreSprite.Position, pixelSize,2, 4);
    }
  }

  private void CreateMissile(int missileDelay, double missileStep) {
    // Define a missile sprite
    playerMissile.Extent =  gameArea;
    playerMissile.SpriteRect = missileSheet.GetSpriteRectAt(0,0);
    playerMissile.setPixelSize(pixelSize);
    playerMissile.Movement.Direction = Movement.MovementDirection.Up;
    playerMissile.Movement.Style = Movement.MovementStyle.Limit;
    playerMissile.Movement.delay = missileDelay;
    playerMissile.Movement.setMovementStep((int)(missileStep));
    playerMissile.Animation.Frames = new Point[1] ;
    playerMissile.Animation.Frames[0] = new Point(0,0);
    playerMissile.Position = new Point(0,gameArea.bottom-( (int)(10 * pixelSize) ));
    playerMissile.DeathAnimation.FrameDelay = 5;
    playerMissile.DeathAnimation.Loop = false;
    playerMissile.DeathAnimation.Frames = new Point[1];
    playerMissile.DeathAnimation.Frames[0] = new Point(0,0);
    playerMissile.Movement.Complete = true;
  }


}
