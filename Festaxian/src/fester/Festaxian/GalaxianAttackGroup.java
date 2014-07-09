package fester.Festaxian;

import android.graphics.Point;
import android.graphics.Rect;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: RLester
 * Date: 04/06/12
 * Time: 13:57
 * To change this template use File | Settings | File Templates.
 */
public class GalaxianAttackGroup {

  public Galaxian BaseShip;

  public Galaxian WingManLeft;

  public Galaxian WingManRight;

  public Galaxian coreShip = new Galaxian();

  public GalaxianAttackGroup() {
    Random r = new Random();
    coreShip.AttackTimer.delay = 12000 + r.nextInt(8000); // up to 20 Second attack interval;
    coreShip.attackMovement.delay = 2;
    coreShip.attackMovement.Style = Movement.MovementStyle.Bezier;
    coreShip.attackMovement.Direction = Movement.MovementDirection.Bezier;
    coreShip.attackMovement.setMovementPixelSize(2);
    coreShip.attackMovement.setMovementStep(2);
    InitDeathSprite();
  }

  public void Update(boolean canAttack) {
    // Reset the timer so the attack delay resets
    // until "canAttack" is true. Can be used to prevent two groups attacking at once
    if (!canAttack)
      coreShip.AttackTimer.Reset();
    if (coreShip.AttackTimer.Expired() && canAttack)
      Attack();
    else {
      BaseShip.Update();
      WingManLeft.Update();
      WingManRight.Update();
    }

    BaseShipDeathScoreSprite.AdvanceFrame();

    if (coreShip.attackMovement.CanMove())
      Move();
  }

  // Trigger all three  to attack simultaneously
  public void Attack() {
    // Setup the attack settings (offset) will end the bezier attack curve at the correct position
    coreShip.Position = new Point();
    coreShip.Position.x = BaseShip.Position.x;
    coreShip.Position.y = BaseShip.Position.y;
    coreShip.setPixelSize(BaseShip.PixelSize());
    coreShip.Player = BaseShip.Player;
    coreShip.Extent = BaseShip.Extent;
    coreShip.SpriteRect = BaseShip.SpriteRect;

    try {
      SetupAttack(coreShip,0, false);
    }
    catch(Exception ex)  {
      System.out.printf("Exception")  ;
    }

    SetupAttack(BaseShip, 0, true);
    SetupAttack(WingManLeft, WingManLeft.Position.x-BaseShip.Position.x, true);
    SetupAttack(WingManRight, BaseShip.Position.x-WingManRight.Position.x, true);

    // Save the attack group state before the attack
    // so the score for the base ship hit can be worked out later
    wingManLeftDead = WingManLeft.Dead;
    wingManRightDead = WingManRight.Dead;

    WingManLeft.attackMovement.XOffset = -32;
    WingManLeft.attackMovement.YOffset = +32;
    WingManRight.attackMovement.XOffset = +32;
    WingManRight.attackMovement.YOffset = +32;
  }

  private boolean wingManLeftDead = false;
  private boolean wingManRightDead = false;

  public boolean IsAttacking() {
    if (
        ((WingManLeft.getStatus() == Galaxian.AttackStatus.Attacking && !WingManLeft.Dead)
        || (WingManRight.getStatus() == Galaxian.AttackStatus.Attacking  && !WingManRight.Dead)
        || (BaseShip.getStatus() == Galaxian.AttackStatus.Attacking)
        )
        && BaseShip.Dead == false
      )
      return true;
    else
      return false;
  }

  public void AssignAttackShips(Galaxian base, Galaxian left, Galaxian right) {
    WingManLeft = left;
    WingManLeft.Grouped = true;
    WingManLeft.UseCustomBezierPath = true;

    WingManRight = right;
    WingManRight.Grouped = true;
    WingManRight.UseCustomBezierPath = true;

    BaseShip = base;
    BaseShip.Grouped = true;
    BaseShip.UseCustomBezierPath = true;

    BaseShipDeathScoreSprite.Dying = false;
    BaseShipDeathScoreSprite.Dead = false;
  }

  private void SetupAttack(Galaxian ship, int xOffset, boolean update) {
    if (!ship.IsDyingOrDead()) {
      ship.AttackTimer.Enabled = true;
      ship.AttackTimer.Expired() ;
      ship.attackMovement.BezierStart = ship.Position;
      ship.attackMovement.BezierCP1 = new Point(ship.Player.Position.x-20,ship.Position.y + ( (ship.Player.Position.y - ship.Position.y) / 3)) ;
      ship.attackMovement.BezierCP2 = new Point(ship.Player.Position.x-20,ship.Player.Position.y - ( (ship.Player.Position.y - ship.Position.y) / 3)) ;
      ship.attackMovement.BezierEnd = new Point(ship.Player.Position.x + xOffset, ship.Player.Position.y) ;
      if (update)
        ship.Update(true);
    }
  }

  public boolean IsAttackGroupShip(Galaxian ship) {
    boolean bAttack = false;
    if (ship == BaseShip)
      bAttack = true;
    if (ship == WingManLeft && WingManLeft.getStatus() != Galaxian.AttackStatus.Formation && WingManLeft.Grouped)
      bAttack = true;
    if (ship == WingManRight &&  WingManRight.getStatus() != Galaxian.AttackStatus.Formation && WingManRight.Grouped)
      bAttack = true;
    return bAttack;
  }

  public Sprite BaseShipDeathScoreSprite = new Sprite();

  private void InitDeathSprite() {
    BaseShipDeathScoreSprite.Animation = new Animation();
    BaseShipDeathScoreSprite.Animation.Frames = new Point[1] ;
    BaseShipDeathScoreSprite.Animation.Frames[0] = new Point(0,0);

    BaseShipDeathScoreSprite.DeathAnimation =  new Animation();
    BaseShipDeathScoreSprite.DeathAnimation.FrameDelay = 200;
    BaseShipDeathScoreSprite.DeathAnimation.Loop = false;
    BaseShipDeathScoreSprite.DeathAnimation.Frames = new Point[4];
    BaseShipDeathScoreSprite.DeathAnimation.Frames[0] = new Point(2,0);
    BaseShipDeathScoreSprite.DeathAnimation.Frames[1] = new Point(2,0);
    BaseShipDeathScoreSprite.DeathAnimation.Frames[2] = new Point(2,0);
    BaseShipDeathScoreSprite.DeathAnimation.Frames[3] = new Point(2,0);

    BaseShipDeathScoreSprite.Position = new Point(0,0);
  }

  public void SetDeathAnimationPoints() {
    int frame = 2;
    if ( (WingManLeft.IsDyingOrDead() && !wingManLeftDead) && (WingManRight.IsDyingOrDead() && !wingManRightDead) ) {
      frame = 5; // 800 - Both wingmen shot before base ship
      BaseShip.setScore(800);
    }
    else
      if (!WingManLeft.IsDyingOrDead() && !WingManRight.IsDyingOrDead())   {
        frame = 2; // 150 - Base ship shot first
        BaseShip.setScore(150);
      }
      else {
        if (!wingManLeftDead || !wingManRightDead)
        frame = 4; // 300 - One wingman killed before the base ship
        BaseShip.setScore(300);
      }

    BaseShipDeathScoreSprite.DeathAnimation.Frames[0].x = frame;
    BaseShipDeathScoreSprite.DeathAnimation.Frames[1].x = frame;
    BaseShipDeathScoreSprite.DeathAnimation.Frames[2].x = frame;
    BaseShipDeathScoreSprite.DeathAnimation.Frames[3].x = frame;
  }

  private void Move() {
    if (BaseShip.Dead && !BaseShipDeathScoreSprite.IsDyingOrDead())  {
      SetDeathAnimationPoints();
      BaseShipDeathScoreSprite.Dying = true;
      BaseShipDeathScoreSprite.Position = new Point(BaseShip.Position.x,  BaseShip.Position.y);
      BaseShipDeathScoreSprite.setPixelSize(BaseShip.PixelSize());
      BaseShipDeathScoreSprite.SpriteRect
              = new Rect(
              BaseShip.SpriteRect.left,
              BaseShip.SpriteRect.top,
              BaseShip.SpriteRect.right,
              BaseShip.SpriteRect.bottom
      );
    }
    if (BaseShipDeathScoreSprite.Dead) {
      BaseShipDeathScoreSprite.DeathAnimation.Reset();
    }

    if (WingManLeft.getStatus() == Galaxian.AttackStatus.Docking && BaseShip.IsDyingOrDead())  {
      WingManLeft.Grouped = false;
      WingManLeft.UseCustomBezierPath = false;
    }
    if (WingManRight.getStatus() == Galaxian.AttackStatus.Docking && BaseShip.IsDyingOrDead())  {
      WingManRight.Grouped = false;
      WingManRight.UseCustomBezierPath = false;
    }

    BaseShip.Move(true);

    if (WingManLeft.Grouped)
      WingManLeft.Move(true);
    if (WingManRight.Grouped)
      WingManRight.Move(true);

    if (WingManLeft.getStatus() == Galaxian.AttackStatus.Attacking && WingManLeft.Grouped) {
      WingManLeft.Position.x = BaseShip.Position.x - (int)(WingManLeft.PixelSize() * 11) ;
      WingManLeft.Position.y = BaseShip.Position.y - (int)(WingManLeft.PixelSize() * 14) ;
      WingManLeft.AttackAngle = BaseShip.attackMovement.attackAngle;
    }
    if (WingManRight.getStatus() == Galaxian.AttackStatus.Attacking && WingManRight.Grouped) {
      WingManRight.Position.x = BaseShip.Position.x + (int)(WingManRight.PixelSize() * 11);
      WingManRight.Position.y = BaseShip.Position.y - (int)(WingManRight.PixelSize() * 14);
      WingManRight.AttackAngle = BaseShip.attackMovement.attackAngle;
    }
  }

}
