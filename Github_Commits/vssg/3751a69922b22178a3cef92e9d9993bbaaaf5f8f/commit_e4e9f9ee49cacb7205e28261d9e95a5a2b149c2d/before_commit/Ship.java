package com.game.vssg;

import static com.game.vssg.VSSG.WORLD_CONSTANT;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectSet;

import java.util.Random;

public class Ship extends Sprite {

    private float speed;
    private boolean active;
    private ActionState actionState;
    private Faction faction;
    private int actionCounter;
    private boolean isIdle;
    private boolean flag = false;
    private ActionState previousActionState;
    private ObjectSet<Ship> targets;

    private final Vector2 position;
    private final Rectangle hitbox;
    private final ShapeRenderer shapeRenderer;
    private final float half = 0.5f;
    private final float angleCalc = 360;


    enum Faction {
        PURPLE,
        TEAL
    }


    enum ActionState {
        PLAYER_CONTROL,
        CRUISE,
        FIRE,
        LEFT_U_TURN,
        RIGHT_U_TURN,
        CIRCLE,
        QUARTER_LEFT_TURN,
        QUARTER_RIGHT_TURN,
        IDLE,
        STOP,
        ATTACK

    }

    public Ship(Texture texture, Vector2 position, float speed, ActionState actionState, ActionState previousActionState, Faction faction, ObjectSet<Ship> targets) {
        super(texture);
        this.position = position;
        this.speed = speed;
        this.actionState = actionState;
        this.previousActionState = previousActionState;
        this.hitbox = new Rectangle();
        this.shapeRenderer = new ShapeRenderer();
        this.actionCounter = 0;
        this.faction = faction;
        this.targets = targets;
        this.active = true;

    }

    // Determining the next position of the ship every frame.
    public void update(float delta, Ship ship, long WORLD_WIDTH, long WORLD_HEIGHT) {

        if (active) {
            Vector2 velocity = new Vector2(speed, 0).setAngleDeg(getRotation());
            position.add(velocity.x * delta, velocity.y * delta);

            // Check if the ship is out of screen bounds and deactivate it if necessary
            if (position.x > WORLD_WIDTH || position.y > WORLD_HEIGHT) {
                active = false;
            }
            // Update the sprite's position
            setPosition(position.x, position.y);

            updateHitBox(ship);
        }

        if (ship.position.x >= WORLD_CONSTANT - 500 || ship.position.y >= WORLD_CONSTANT - 500) {
            if (!this.flag) {

                ship.setActionState(ActionState.LEFT_U_TURN, ship.previousActionState);
                this.flag = true;
                System.out.println("Obstacle avoidance engaged");
            }

            else {
                ship.setActionState(ActionState.CRUISE, ship.previousActionState);
                System.out.println("Obstacle avoidance engaged2222");

                this.flag = false;
            }
        }




    }


    public void setFaction(Faction faction) {

        this.faction = faction;
    }

    public Faction getFaction() {

        return this.faction;
    }

    public float getSpeed() {

        return this.speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;

    }


    public Laser fireLaser(Texture texture, Ship ship) {
        float offsetX = -1.5f;
        float offsetY = -2.25f;
        ship.setOrigin(ship.getOriginX(), ship.getOriginY());
        Vector2 laserPosition = new Vector2(ship.getX() + ship.getOriginX() + offsetX, ship.getY() + ship.getOriginY() + offsetY);
        Laser laser = new Laser(texture, laserPosition.x, laserPosition.y, ship.getRotation(), 2048, hitbox, 0, ship);
        laser.setOrigin(0, laser.getOriginY() / 2);
        return laser;
    }


    // Update the bounding box based on the scaled sprite's position and size
    private void updateHitBox(Ship ship) {
        float shipScale = VSSG.shipScale;
        float scaledWidth = ship.getWidth() * shipScale;
        float scaledHeight = ship.getHeight() * shipScale;

        // Update the bounding box's position and size to match the scaled sprite
        float hitboxOffset = 0;
        hitbox.set(ship.getX() + hitboxOffset, ship.getY() + hitboxOffset, scaledWidth, scaledHeight);
    }

    void handleActionState(Ship ship, Texture greenLaserTexture, Texture redLaserTexture, Texture blueLaserTexture, ObjectSet<Laser> lasers, Sound laserBlast) {
        ship.handleIdle(ship);
        ship.handleLeftUTurn(ship);
        ship.handleRightUTurn(ship);
        ship.handleCircle(ship);
        ship.handleQuarterLeftTurn(ship);
        ship.handleQuarterRightTurn(ship);
        ship.handleStop(ship);
        ship.handleCruise(ship);
        ship.handleFire(ship, greenLaserTexture, redLaserTexture, blueLaserTexture, lasers, laserBlast);
        ship.handleAttack(ship);

    }

    public void handleCruise(Ship ship) {
        if (ship.getActionState() == Ship.ActionState.CRUISE) {


            if (ship.getActionCounter() <= 2048) {
                ship.setActionCounter(ship.getActionCounter() + 1);
            } else if (ship.getActionCounter() > 2048) {
                if (ship.isIdle) {
                    ship.setActionState(ActionState.IDLE, ActionState.CRUISE);
                    ship.setActionCounter(0);
                } else {
                    ship.setActionState(Ship.ActionState.IDLE, ActionState.CRUISE);
                    ship.setActionCounter(0);
                }
            }
        }
    }

    public void handleLeftUTurn(Ship ship) {
        if (ship.getActionState() == Ship.ActionState.LEFT_U_TURN) {
            if (ship.getActionCounter() <= angleCalc) {
                ship.setActionCounter(ship.getActionCounter() + 1);
                ship.rotate(half);
            } else if (ship.getActionCounter() > angleCalc) {
                if (ship.isIdle) {
                    ship.setActionState(ActionState.IDLE, ActionState.LEFT_U_TURN);
                    ship.setActionCounter(0);
                } else {
                    ship.setActionState(Ship.ActionState.IDLE, ActionState.LEFT_U_TURN);
                    ship.setActionCounter(0);
                }
            }

        }
    }

    public void handleRightUTurn(Ship ship) {
        if (ship.getActionState() == Ship.ActionState.RIGHT_U_TURN) {
            if (ship.getActionCounter() <= angleCalc) {
                ship.setActionCounter(ship.getActionCounter() + 1);
                ship.rotate(-half);
            } else if (ship.getActionCounter() > angleCalc) {
                if (ship.isIdle) {
                    ship.setActionState(ActionState.IDLE, ActionState.RIGHT_U_TURN);
                    ship.setActionCounter(0);
                } else {
                    ship.setActionState(Ship.ActionState.IDLE, ActionState.RIGHT_U_TURN);
                    ship.setActionCounter(0);
                }
            }
        }
    }

    public void handleCircle(Ship ship) {
        if (ship.getActionState() == ActionState.CIRCLE) {
            if (ship.getActionCounter() <= angleCalc * 4) {
                ship.setActionCounter(ship.getActionCounter() + 1);
                ship.rotate(0.125f);
            } else if (ship.getActionCounter() > angleCalc * 4) {
                if (ship.isIdle) {
                    ship.setActionState(ActionState.IDLE, ActionState.CIRCLE);
                    ship.setActionCounter(0);
                } else {
                    ship.setActionState(Ship.ActionState.IDLE, ActionState.CIRCLE);
                    ship.setActionCounter(0);
                }
            }
        }
    }

    public void handleQuarterLeftTurn(Ship ship) {
        if (ship.getActionState() == ActionState.QUARTER_LEFT_TURN) {
            if (ship.getActionCounter() <= 180 * 4) {
                ship.setActionCounter(ship.getActionCounter() + 1);
                ship.rotate(0.25f);
            } else if (ship.getActionCounter() > 180 * 4) {
                if (ship.isIdle) {
                    ship.setActionState(ActionState.IDLE, ActionState.QUARTER_LEFT_TURN);
                    ship.setActionCounter(0);
                } else {
                    ship.setActionState(Ship.ActionState.IDLE, ActionState.QUARTER_LEFT_TURN);
                    ship.setActionCounter(0);
                }
            }
        }
    }

    public void handleQuarterRightTurn(Ship ship) {
        if (ship.getActionState() == ActionState.QUARTER_RIGHT_TURN) {
            if (ship.getActionCounter() <= 180 * 4) {
                ship.setActionCounter(ship.getActionCounter() + 1);
                ship.rotate(-0.25f);
            } else if (ship.getActionCounter() > 180 * 4) {
                if (ship.isIdle) {
                    ship.setActionState(ActionState.IDLE, ActionState.QUARTER_RIGHT_TURN);
                    ship.setActionCounter(0);
                } else {
                    ship.setActionState(Ship.ActionState.IDLE, ActionState.QUARTER_RIGHT_TURN);
                    ship.setActionCounter(0);
                }
            }
        }
    }



    public void handleStop(Ship ship) {
        if (ship.getActionState() == ActionState.STOP) {
            if (ship.getActionCounter() <= angleCalc) {
                ship.setActionCounter(ship.getActionCounter() + 1);
                ship.setSpeed(0);
            } else if (ship.getActionCounter() > angleCalc) {
                ship.setActionState(ship.previousActionState, ActionState.STOP);
                ship.setActionCounter(0);
            }
        }
    }


    public float getRandomSpeed() {
        Random rand = new Random();
        int randInt = rand.nextInt(10);

        if (randInt == 1) {
            this.setSpeed(50);
        } else if (randInt == 2) {
            this.setSpeed(70);
        } else if (randInt == 3) {
            this.setSpeed(80);
        } else if (randInt == 4) {
            this.setSpeed(90);
        } else if (randInt == 5) {
            this.setSpeed(100);
        } else if (randInt == 6) {
            this.setSpeed(110);
        } else {
            this.setSpeed(120);
        }


        return this.speed;
    }


    public void handleIdle(Ship ship) {

        if (ship.getActionState() == ActionState.IDLE) {
            ship.isIdle = true;
            ship.setSpeed(ship.getRandomSpeed());
            if (ship.actionCounter <= 0) {
                Random rand = new Random();
                int rand_int = rand.nextInt(10);
                if (rand_int == 1) {
                    ship.setActionState(ActionState.LEFT_U_TURN, ship.actionState);
                } else if (rand_int == 2) {
                    ship.setActionState(ActionState.CIRCLE, ship.actionState);
                } else if (rand_int == 3) {
                    ship.setActionState(ActionState.QUARTER_LEFT_TURN, ship.actionState);
                } else if (rand_int == 4) {
                    ship.setActionState(ActionState.STOP, ship.actionState);
                    System.out.println("STOPPED");

                } else if (rand_int == 5) {
                    ship.setActionState(ActionState.QUARTER_RIGHT_TURN, ship.actionState);
                } else if (rand_int == 6) {

                    ship.setActionState(ActionState.RIGHT_U_TURN, ship.actionState);
                    System.out.println("CRUISING");
                } else {
                    ship.setActionState(ActionState.CRUISE, ship.actionState);
                    System.out.println("CRUISING");

                }
            }

        }
    }


    public void handleFire(Ship ship, Texture greenLaserTexture, Texture blueLaserTexture, Texture redLaserTexture, ObjectSet<Laser> lasers, Sound laserBlast) {
        if (ship.getActionState() == ActionState.FIRE) {
            if (ship.getActionCounter() <= 100) {

                actionCounter++;

            } else if (ship.getActionCounter() > 100) {

                Laser laser = ship.fireLaser(redLaserTexture, ship);
                laser.setShip(ship);
                lasers.add(laser);
                laserBlast.play(2f);
                ship.setActionCounter(0);

                ship.setActionState(ship.previousActionState, ship.actionState);

            }
        }
    }


    float getTargetAngle(Ship sourceShip, Ship targetShip) {

        Vector2 sourcePos = new Vector2(sourceShip.getX(), sourceShip.getY());
        Vector2 targetPos = new Vector2(targetShip.getX(), targetShip.getY());

        // Calculate the direction vector from source to target
        Vector2 direction = targetPos.sub(sourcePos).nor();

        // Calculate the angle between the source and target ships
        return direction.angleDeg();

    }


    public void rotateTowardTarget(Ship sourceShip, Ship targetShip, float rotationSpeed, float deltaTime) {
        // Get the positions of the source and target ships and
        // calculate the angle between the source and target ships
        float targetAngle = getTargetAngle(sourceShip, targetShip);

        // Get the current rotation of the source ship
        float currentAngle = sourceShip.getRotation();

        // Calculate the shortest angle difference between the current and target angles
        float angleDifference = Math.abs(targetAngle - currentAngle);
        if (angleDifference > 180) {
            angleDifference = 360 - angleDifference;
        }

        // Calculate the amount to rotate based on rotationSpeed and deltaTime
        float rotateAmount = rotationSpeed * deltaTime;

        // Determine the direction of rotation (clockwise or counterclockwise)
        if (angleDifference > rotateAmount) {
            if ((targetAngle - currentAngle + 360) % 360 > 180) {
                sourceShip.rotate(-rotateAmount);
            } else {
                sourceShip.rotate(rotateAmount);
            }
        } else {
            sourceShip.setRotation(targetAngle);
        }
    }


    public Rectangle getHitbox() {

        return this.hitbox;

    }

    public boolean isActive() {

        return active;
    }

    public void setInactive(Ship ship) {
        ship.active = false;

    }

    public void drawBoundingBox() {

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);

        float x = hitbox.x;
        float y = hitbox.y;
        float width = hitbox.width;
        float height = hitbox.height;

        shapeRenderer.rect(x, y, width, height);

        shapeRenderer.end();
    }

    // Dispose of the ShapeRenderer when it's no longer needed
    public void dispose() {
        shapeRenderer.dispose();
    }


    public ShapeRenderer getShapeRenderer() {

        return this.shapeRenderer;
    }

    public ActionState getActionState() {
        return this.actionState;

    }

    public void setActionState(ActionState actionState, ActionState previousActionState) {
        this.actionState = actionState;
        this.previousActionState = previousActionState;

    }


    public int getActionCounter() {
        return this.actionCounter;

    }

    public void setActionCounter(int actionCounter) {
        this.actionCounter = actionCounter;

    }

    public float subtractSmallerFromLarger(float a, float b) {
        float larger = Math.max(a, b);
        float smaller = Math.min(a, b);
        return larger - smaller;
    }

    public void detectTargets(Ship targetShip, ObjectSet<Ship> targets) {

        if (targetShip.faction != this.faction) {
        if ((subtractSmallerFromLarger(targetShip.getX(), this.getX())) < 2000 || (subtractSmallerFromLarger(targetShip.getY(), this.getY())) < 2000) {
            if (!targets.contains(targetShip)) {
                targets.add(targetShip);
                System.out.println("Target Acquired!");
            }
        }
        }
    }

    void handleAttack(Ship ship) {

        if (ship.getActionState() == ActionState.ATTACK) {
// Tells the specified ship to pick a target and shoot at it until it is destroyed or out of range.
            if (!seekDestroy(ship)) {
                ship.setActionState(ActionState.IDLE, ship.actionState);
            }

            }

    }

    boolean seekDestroy(Ship ship) {
        boolean alive = false;
        if (ship.targets.size > 0) {
            //offset = range of how far off center ship will fire.
            float offset = 5;
            Ship target = ship.targets.first();
            float targetAngle = getTargetAngle(ship, target);
            if (ship.getActionCounter() != targetAngle) {

                if (ship.getRotation() < getTargetAngle(ship, target) - offset || ship.getRotation() > getTargetAngle(ship, target) + offset) {
                    actionCounter++;
                    ship.rotateTowardTarget(ship, target, 200, Gdx.graphics.getDeltaTime());
                } else if (ship.getRotation() >= getTargetAngle(ship, target) - offset || ship.getRotation() <= getTargetAngle(ship, target) + offset) {

                    if (target.active) {
                        ship.setActionState(ActionState.FIRE, ship.actionState);
                        ship.setActionCounter(0);
                    }
                }
            }

            alive = target.active;
        }
        return alive;
    }

    public ObjectSet<Ship> getTargets() {

        return this.targets;
    }


}