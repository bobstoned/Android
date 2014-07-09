package fester.Festaxian;

import android.graphics.Point;
import android.graphics.Rect;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: RLester
 * Date: 25/04/12
 * Time: 00:51
 * To change this template use File | Settings | File Templates.
 */
public class SpriteFormation {

  private int cols;
  private int rows;
  public Rect Bounds ;
  //private Point origin = new Point(10,40);
  public int gap = 2;
  private Galaxian[][] matrix ;
  public Movement movement = new Movement() ;
  public double pixelSize=1;

  public SpriteFormation(Rect bounds, int cols, int rows, double pixelSize) {
    this.Bounds = bounds;
    this.cols = cols;
    this.rows = rows;
    this.pixelSize = pixelSize;
    matrix = new Galaxian[cols][rows];
    movement.Style = Movement.MovementStyle.Cyclic;
  }

  public int Rows() {
    return rows;
  }

  public int Cols() {
    return cols;
  }

  public void Move() {
    Galaxian sprite;
    // Has the formation movement delay expired
    // Must move all sprites in one go (or formation will break up)
    boolean canMoveAll = false;
      canMoveAll = this.movement.CanMove();
    if (canMoveAll)
      for(int w=0; w<=cols-1; w++)
       for(int h=0; h<=rows-1; h++)  {
         sprite = matrix[w][h];
         if (sprite != null && !sprite.Drawing && !sprite.Dead && !sprite.Dying)
            sprite.MoveFormation();
       }
    // Select which ship will attack next
    if (AllDocked()) {
        DisableAttack();
        EnableAttack();
    }
  }

  // Are all galaxians docked in formation
  public boolean AllDocked() {
    Galaxian sprite;
    for(int c=0; c<cols; c++)
      for(int r=0; r<rows; r++) {
        sprite =  matrix[c][r];
        if (sprite != null) {
          if (sprite.getStatus() != Galaxian.AttackStatus.Formation && !sprite.Dead)
            return false;
        }
      }
    return true ;
  }

  // Prevent any more attacks by the fleet
  public void DisableAttack() {
    Galaxian sprite;
    for(int c=0; c<cols; c++)
      for(int r=0; r<rows; r++) {
        sprite =  matrix[c][r];
        if (sprite != null) {
          sprite.AttackTimer.Enabled = false;
        }
      }
  }

  // Enable the fleet to attack
  public void EnableAttack() {
    Random ran = new Random();
    Galaxian sprite;
    int leftEdge = GetLeftEdge();
    int rightEdge = GetRightEdge();
    Galaxian leftAttackShip = GetAttackShip(leftEdge) ;
    Galaxian rightAttackShip = GetAttackShip(rightEdge) ;

    for(int c=0; c<cols; c++)
      for(int r=0; r<rows; r++) {
        sprite =  matrix[c][r];
        if (sprite != null && !sprite.Dead && !sprite.Dying) {
          // Set a random attack delay for the galaxian
          sprite.AttackTimer.delay = ran.nextInt(5000) + 8000;
          sprite.AttackTimer.Expired();
        }
      }
    if (leftAttackShip != null && !leftAttackShip.Grouped)
      leftAttackShip.AttackTimer.Enabled = true;
    if (rightAttackShip != null && !leftAttackShip.Grouped)
      rightAttackShip.AttackTimer.Enabled = true;
  }

  public int GetLeftEdge() {
    int leftEdge = 1000;
    Galaxian sprite;
    for(int c=0; c<cols; c++)
      for(int r=0; r<rows; r++) {
        sprite =  matrix[c][r];
        if (sprite != null && !sprite.Dead && !sprite.Dying) {
          // find the first column of active ships
          if (c < leftEdge)
            leftEdge = c;
        }
      }
    return leftEdge;
  }

  public int GetRightEdge() {
    int rightEdge = 0;
    Galaxian sprite;
    for(int c=0; c<cols; c++)
      for(int r=0; r<rows; r++) {
        sprite =  matrix[c][r];
        if (sprite != null && !sprite.Dead && !sprite.Dying) {
          // find the first column of active ships
          if (c > rightEdge)
            rightEdge = c;
        }
      }
    return rightEdge;
  }

  public Galaxian GetAttackShip(int col) {
    Galaxian sprite;
    if (col < cols) {
      for(int row=0; row<rows;row++) {
        sprite =  matrix[col][row];
        if (sprite != null && !sprite.Dead && !sprite.Dying)
          return sprite;
      }
    }
    return null;
  }

  public Galaxian[][] getMatrix() {
    return matrix;
  }

  public void PositionSprites(int width, int height) {
    Galaxian sprite;
    for(int c=0; c<cols; c++)
      for(int r=0; r<rows; r++) {
        sprite =  matrix[c][r];
        if (width == 0 || height ==0) {
          // Use the native dimensions of the image to draw it to the canvas
          width = sprite.logicalWidth;
          height = sprite.logicalHeight;
        }
        if (sprite != null) {
          sprite.Movement.Direction = Movement.MovementDirection.Right;
          sprite.Movement.Style = Movement.MovementStyle.Cyclic;
          sprite.Movement.delay = this.movement.delay;
          sprite.Position = new Point(Bounds.left + (c * (int)((width+gap) * sprite.PixelSize()) ), Bounds.top + (r * (int)((width+gap)* sprite.PixelSize()) ));
          sprite.formationPosition.x = sprite.Position.x;
          sprite.formationPosition.y = sprite.Position.y;
          sprite.setStatus(Galaxian.AttackStatus.Formation);
        }
      }
  }

  // Decided on reversing the formation direction
  // should a ship hit the game area boundary
  public void SetFormationDirection(Rect gameArea) {
    int leftEdge = GetLeftEdge();
    Galaxian leftAttackShip = GetAttackShip(leftEdge) ;
    Rect fRect = GetBoundingRect();
    Movement.MovementDirection gDir = Movement.MovementDirection.Left;
    boolean reverseDir = false;

    if (fRect.right >= gameArea.right && leftAttackShip.Movement.Direction == Movement.MovementDirection.Right) {
      gDir = Movement.MovementDirection.Left;
      reverseDir = true;
    }
    if (fRect.left <= gameArea.left && leftAttackShip.Movement.Direction == Movement.MovementDirection.Left) {
      gDir = Movement.MovementDirection.Right;
      reverseDir = true;
    }

    if (reverseDir) {
      Galaxian sprite;
      for(int c=0; c<cols; c++)
        for(int r=0; r<rows; r++) {
          sprite =  matrix[c][r];
          if (sprite != null) {
            sprite.Movement.Direction = gDir;
          }
        }
    }
  }

  public Rect GetBoundingRect() {
    int max_x = 0;
    int max_y = 0;
    int min_y = 10000;
    int min_x = 10000;
    Rect rect = new Rect();
    Rect boundRect ;
    Galaxian galaxian;
    for(int w=0; w<cols; w++)
      for(int h=0; h<rows; h++) {
        galaxian = matrix[w][h] ;
        if (galaxian != null && !galaxian.Dead) {
          boundRect = matrix[w][h].GetFormationBoundingBox(16, 16);
          if (boundRect.right > max_x)
            max_x = boundRect.right;
          if (boundRect.left < min_x)
            min_x = boundRect.left;
          if (boundRect.bottom > max_y)
            max_y = boundRect.bottom;
          if (boundRect.top < min_y)
            min_y = boundRect.bottom;
        }
      }
    rect.left = min_x;
    rect.right = max_x;
    rect.top = min_y;
    rect.bottom = max_y;
    return rect;
  }

  public int getGap() {
    return gap;
  }

  public void setGap(int gap) {
    this.gap = gap;
  }
}
