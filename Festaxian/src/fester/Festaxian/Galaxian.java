package fester.Festaxian;

import android.graphics.*;

public class Galaxian extends Sprite {

  private int score = 0;

  private AttackStatus status = AttackStatus.Formation ;

  private ShipType shipType = ShipType.Drone;

  private Animation diveAnimation = new Animation();

  private Animation dockingAnimation = new Animation();

  public Animation AttackAnimation = new Animation();

  private int maxMissiles = 6;

  public Sprite missiles[] = new Sprite[maxMissiles] ;

  public int missileCount = 0;

  public Point missileFrame;

  public Rect missileRect ;

  public Sprite Player;

  public Point formationPosition = new Point();

  public Movement attackMovement = new Movement();
  public Movement dockingMovement = new Movement();

  // How often to fire a missile
  private SpriteTimer fireMissileTimer = new SpriteTimer();

  public Galaxian() {
    CreateAttackAngleMappings();
    attackMovement.Direction = fester.Festaxian.Movement.MovementDirection.Bezier;
    attackMovement.Style = fester.Festaxian.Movement.MovementStyle.Bezier;
    attackMovement.setMovementPixelSize((int)PixelSize());
    attackMovement.delay = 2;

    dockingMovement.Direction = fester.Festaxian.Movement.MovementDirection.Down;
    dockingMovement.Style = fester.Festaxian.Movement.MovementStyle.Limit;
    dockingMovement.setMovementPixelSize((int)PixelSize());
    dockingMovement.delay = 15;

    fireMissileTimer.delay = 500;

    getDiveAnimation().Loop = false;
  }

  public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }

  public ShipType getShipType() {
    return shipType;
  }

  public void setShipType(ShipType shipType) {
    this.shipType = shipType;
    switch (this.shipType) {
      case Base :
        score = 60;
        break;
      case BodyGuard:
        score = 50;
        break;
      case Raider:
        score = 40;
        attackMovement.setMovementPixelSize(3);
        attackMovement.delay = 10;
        break;
      case Drone:
        score = 30;
        break;
    }
  }


  // How often to initiate an attack
  public SpriteTimer AttackTimer = new SpriteTimer();

  public boolean UseCustomBezierPath = false;

  public boolean Grouped = false;

  public void Update() {
    Update(false);
  }

  // Update the state of the galaxian
  public void Update(boolean attackNow) {
    int bezOffset ;
    int bezMagnitude;
    if (AttackTimer.Enabled) {
      if ( ((AttackTimer.Expired() && !Grouped)||attackNow) && status == AttackStatus.Formation && !Player.IsDyingOrDead())  {
        fireMissileTimer.Enabled = true;
        this.formationPosition.x = this.Position.x;
        this.formationPosition.y = this.Position.y;
        // reset dive animation
        diveAnimation.Reset();
        attackMovement.Extent = this.Extent;
        // The raiders dive in a more extreme arc
        if (this.shipType == ShipType.Raider)
          bezMagnitude = 200;
        else
          bezMagnitude = 50;
        // Select the lean of the curve left.right
        // depending on the relative positions of the player ship and the Galaxian
        if (this.Player.Position.x < this.Position.x)
          bezOffset = -1 * bezMagnitude;
        else
          bezOffset = +1 * bezMagnitude;

        if (!UseCustomBezierPath) {
          attackMovement.BezierStart = this.Position;
          attackMovement.BezierCP1 = new Point(this.Player.Position.x+bezOffset,this.Position.y + ( (this.Player.Position.y - this.Position.y) / 3)) ;
          attackMovement.BezierCP2 = new Point(this.Player.Position.x+bezOffset,this.Player.Position.y - ( (this.Player.Position.y - this.Position.y) / 3)) ;
          attackMovement.BezierEnd = new Point(this.Player.Position.x+20, this.Player.Position.y) ;
          attackMovement.CreateBezierPath(this.Position, attackMovement.BezierCP1, attackMovement.BezierCP2, attackMovement.BezierEnd);
        }
        else
          attackMovement.CreateBezierPath(this.Position, attackMovement.BezierCP1, attackMovement.BezierCP2, attackMovement.BezierEnd);


        // Reset the dive animation so this will re-start on the next attack
        diveAnimation.Reset();
        setStatus(AttackStatus.Attacking);
      }
    }
    if (status == AttackStatus.Attacking && !IsDyingOrDead() )
      if (fireMissileTimer.Expired() && this.Position.y > (75 * PixelSize()))
         fireMissile();

    if (status == AttackStatus.Attacking && attackMovement.Complete)  {
      setStatus(AttackStatus.Docking);
      // Move the galaxian to the top of the screen above its formation position
      this.Position.x = this.formationPosition.x;
      this.Position.y = 0 ;
      attackMovement.Complete = false;
      // stop firing
      fireMissileTimer.Enabled = false;
      // reset missiles
      InitMissiles();
    }
    if (status == AttackStatus.Docking && dockingMovement.Complete) {
      setStatus(AttackStatus.Formation);
      AttackTimer.Expired();
      dockingMovement.Complete = false;
    }
    this.MoveMissiles();
    this.AdvanceFrame();
  }

  private void InitMissiles() {
    missileCount = 0;
  }

  private void fireMissile() {
    if (missileCount != maxMissiles-1) {
      Sprite missile = new Sprite();
      Rect rect = this.GetBoundingBox();
      missile.Position = new Point(rect.left + rect.width()/2, rect.bottom) ;
      missile.Movement = new Movement();
      missile.Movement.setMovementStep((int)(0.8 * PixelSize()));
      missile.Movement.delay = 20;
      // Which way the missiles will veer (left/right)
      // is dependent on the angle of movement at the
      // the time of firing
      if (attackMovement.attackAngle > 90)
        missile.Movement.XDir = -1;
      else
        missile.Movement.XDir = +1;
      missile.Movement.Direction = fester.Festaxian.Movement.MovementDirection.Down;
      missile.Movement.Style = fester.Festaxian.Movement.MovementStyle.Limit;

      missile.Animation.Frames = new Point[1] ;
      missile.Animation.Frames[0] = missileFrame ;

      missile.SpriteRect = missileRect;
      missile.Extent = this.Extent;
      missiles[missileCount] = missile;
      missileCount++;
    }
  }

  @Override
  public void AdvanceFrame() {
    if (status != AttackStatus.Attacking || this.Dead || this.Dying)
      super.AdvanceFrame();
    else
      diveAnimation.AdvanceFrame();
  }

  public double AttackAngle = 0;

  @Override
  public Point GetFrame() {
    if (status == AttackStatus.Formation || this.Dead || this.Dying)
      return super.GetFrame();
    else
      if (status == AttackStatus.Attacking) {
        if (diveAnimation.Complete)
          try {

            if (AttackAngle == 0)
              // Get frame based on the attack movement
              return AttackAnimation.GetFrame(GetAttackFrameForAngle(attackMovement.attackAngle)) ;
            else
              // Get the frame based on the custom setting in "AttackAngle"
              return AttackAnimation.GetFrame(GetAttackFrameForAngle(AttackAngle)) ;
          }
          catch (Exception ex) {
            return new Point(0,0);
          }
        else
          return diveAnimation.GetCurrentFrame() ;
      }
      else
        if (status == AttackStatus.Docking)
          return dockingAnimation.GetCurrentFrame();
        else
          return super.GetFrame();
  }

  public void DrawFormationBoundingBox(Canvas canvas, int color) {
    Rect posRect = new Rect(formationPosition.x, formationPosition.y,
            formationPosition.x+((int)(SpriteRect.width()*this.PixelSize()))-1,
            formationPosition.y+((int)(SpriteRect.height()*this.PixelSize()))-1);
    Paint paint = new Paint();
    paint.setStyle(Paint.Style.STROKE);
    paint.setColor(color);
    paint.setStrokeWidth(1);
    canvas.drawRect(posRect, paint);
  }

  public Rect GetFormationBoundingBox(int width, int height) {
    if (width == 0 || height == 0) {
      width = SpriteRect.width();
      height = SpriteRect.height();
    }
    Rect posRect = new Rect(formationPosition.x, formationPosition.y, formationPosition.x+((int)(width*this.PixelSize())), Position.y+((int)(height*this.PixelSize())));
    return posRect;
  }

  public void MoveStandard() {
    super.Move(false);
  }

  public void MoveFormation() {
    if (status == AttackStatus.Formation || status == AttackStatus.Docking) {
      super.Move(true);
      // Move by the formation movement
    }
    Movement.Move(this.formationPosition, this.Extent, this.getLogicalRect(), this.PixelSize(), true);
  }

  @Override
  public void Move(boolean moveNow) {
    // Move as long as it isn't dying or dead (stops explosions from floating!)
    if (!IsDyingOrDead() || Grouped )
      switch(getStatus()) {
        case Attacking : {
          // Make sure the attack timer doesn't re-trigger immediately after the attack
          AttackTimer.Expired();
          // Move based on the attack movement
          attackMovement.Move(this.Position, this.Extent, this.SpriteRect, this.PixelSize());
          // Move by the formation movement
          //Movement.Move(this.formationPosition, this.Extent, this.SpriteRect, this.PixelSize());
        }
        break;
        case Docking: {
          Rect dockingRect = new Rect(0, 0, 400, this.formationPosition.y + ( (int)(this.SpriteRect.height()*this.PixelSize())) );
          // Move by the docking movement
          dockingMovement.Move(this.Position, dockingRect, this.SpriteRect, this.PixelSize());
        }
        //case Formation: super.Move(moveNow);
        //break;
      }
  }

  // move active missiles
  public void MoveMissiles() {
    for(int m=0;m<missileCount;m++) {
      missiles[m].Move(false);
      if ( missiles[m].Movement.Complete)
        missiles[m].Dead = true;
    }
  }

  private Point[] attackAngleRanges = new Point[9];

  // Gets the attack animation frame for the current angle of attack
  private int GetAttackFrameForAngle(double angle) {
    int frameNo = -1;
    for(int i=0;i<=8;i++)
      if ((angle >= attackAngleRanges[i].x) && (angle < attackAngleRanges[i].y) ) {
        frameNo = i;
        break;
      }
    return frameNo ;
  }

  // These map angles against galaxian sprites
  private void CreateAttackAngleMappings() {
    attackAngleRanges[0] = new Point(80, 100);  //90
    attackAngleRanges[1] = new Point(100, 120);  //120
    attackAngleRanges[2] = new Point(120, 140);  //140
    attackAngleRanges[3] = new Point(140, 160);  //160
    attackAngleRanges[4] = new Point(160, 180);  //180

    attackAngleRanges[5] = new Point(60, 80);  //60
    attackAngleRanges[6] = new Point(40, 60);  //40
    attackAngleRanges[7] = new Point(20, 40);  //30
    attackAngleRanges[8] = new Point(0, 20);  //0
  }

  public AttackStatus getStatus() {
    return status;
  }

  public void setStatus(AttackStatus status) {
    this.status = status;
  }

  public Animation getDiveAnimation() {
    return diveAnimation;
  }

  public void setDiveAnimation(Animation diveAnimation) {
    this.diveAnimation = diveAnimation;
  }

  public Animation getDockingAnimation() {
    return dockingAnimation;
  }

  public void setDockingAnimation(Animation dockingAnimation) {
    this.dockingAnimation = dockingAnimation;
  }

  public enum AttackStatus {
    Formation,
    Attacking,
    Docking
  }

  public enum ShipType {
    Base,
    BodyGuard,
    Raider,
    Drone
  }

}

