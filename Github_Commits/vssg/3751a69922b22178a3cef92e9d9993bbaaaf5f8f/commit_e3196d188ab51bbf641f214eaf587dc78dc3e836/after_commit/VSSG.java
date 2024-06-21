package com.game.vssg;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Iterator;

public class VSSG implements ApplicationListener {

    // DEBUGGING //
    public static boolean showHitBoxes = false;
    public static boolean mute = false;
    ///////////////

    public static long WORLD_CONSTANT = 32768;
    public static long WORLD_WIDTH = WORLD_CONSTANT;
    public static long WORLD_HEIGHT = WORLD_CONSTANT;
    public static float shipScale = 1f;
    public static float DEFAULT_ZOOM = 2;

    private ObjectSet<PlayerShip> playerShips;
    private ObjectSet<CpuShip> cpuShips;
    private ObjectSet<CpuShip> copiedSet;
    private ObjectSet<Explosion> explosions;
    private ObjectSet<Laser> lasers;
    private SpriteBatch batch;
    private Sound explosionSound1;
    private Sound laserBlast1;
    private Sound laserBlast2;
    private final float worldWidthCentre = (float) WORLD_WIDTH / 2;
    private final float worldHeightCentre = (float) WORLD_HEIGHT / 2;
    private final float wrapDivisor = (float) WORLD_WIDTH / 4096;
    private final float zoomSpeed = 0.002f;

    private Texture purpleShipTexture;
    private Texture greenLaserTexture;
    private Texture explosionTexture1;
    private Texture otherShipTexture;
    private Texture blueLaserTexture;
    private Texture redLaserTexture;
    private Texture greenShipTexture;
    private Texture backgroundTexture;
    private Texture exhaustTexture;
    private Texture tealShipButton;
    private Texture purpleShipButton;

    private OrthographicCamera camera;
    private Viewport viewport;
    private boolean shipSpawnTimeout = false;
    private int shipSpawnCounter = 0;
    private boolean laserSpawnTimeout = false;
    public static boolean playerActive = false;
    private int laserSpawnCounter = 0;

    ////////////////////////////////
    InputProcessor inputManager;

    @Override
    public void create() {
        // Get number of processors for future multithreading purposes.
        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println("Get number of processor cores: " + processors);

        // Load assets.
        purpleShipTexture = new Texture("purple_ship.png");
        otherShipTexture = new Texture("N1.png");
        greenShipTexture = new Texture("teal_ship.png");
        greenLaserTexture = new Texture("laser_green.png");
        redLaserTexture = new Texture("laser_red.png");
        blueLaserTexture = new Texture("laser_blue.png");
        backgroundTexture = new Texture("background.png");
        explosionTexture1 = new Texture("explosion_orange.png");
        exhaustTexture = new Texture("ship_exhaust.png");
        tealShipButton = new Texture("teal_ship_button.png");
        purpleShipButton = new Texture("purple_ship_button.png");
        explosionSound1 = Gdx.audio.newSound(Gdx.files.internal("explosion.wav"));
        laserBlast2 = Gdx.audio.newSound(Gdx.files.internal("laser_blast2.wav"));


        // Covers the playable map in the specified texture.  To be modularized for customization.
        backgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        // Setup camera, viewport, controls input.
        camera = new OrthographicCamera();
        camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);

        camera.zoom = DEFAULT_ZOOM;
        float viewportWidth = Gdx.graphics.getWidth();
        float viewportHeight = Gdx.graphics.getHeight();
        viewport = new ExtendViewport(viewportWidth, viewportHeight, camera);
        viewport.apply();
        Gdx.input.setInputProcessor(inputManager);

        // Prepare SpriteBatch and lists for keeping track of/accessing game objects.
        batch = new SpriteBatch();
        cpuShips = new ObjectSet<>();
        copiedSet = new ObjectSet<>(cpuShips);
        playerShips = new ObjectSet<>();
        explosions = new ObjectSet<>();
        lasers = new ObjectSet<>();

        // Initial ship's details.
        Vector2 vector2 = new Vector2(worldWidthCentre, worldHeightCentre);
        Rectangle hitBox = new Rectangle();
        ObjectSet<Ship> targets = new ObjectSet<>();
        int playerActionCounter = 0;
        PlayerShip playerShip = new PlayerShip(purpleShipTexture, vector2, 40, Ship.ActionState.PLAYER_CONTROL, Ship.ActionState.PLAYER_CONTROL, hitBox, playerActionCounter, Ship.Faction.PURPLE, targets);
        playerShip.setScale(shipScale);
        playerShip.setRotation(0);

        // Add the new ship to the Ship list.
        playerShips.add(playerShip);


    }

    @Override
    public void render() {
       // System.out.println("x = "+camera.position.x+" y = "+camera.position.y);

        float deltaTime = Gdx.graphics.getDeltaTime();
        ScreenUtils.clear(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(camera.combined);
        camera.update();
        handleInput();

        Iterator<PlayerShip> playerIter = playerShips.iterator();
        Iterator<CpuShip> cpuIter = cpuShips.iterator();
        Iterator<Explosion> explosionIter = explosions.iterator();
        Iterator<Laser> laserIter = lasers.iterator();
        Iterator<CpuShip> copyIter = copiedSet.iterator();

        checkIterators(playerIter, explosionIter, cpuIter, copyIter, laserIter, deltaTime);
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT, 0, 0, (int) wrapDivisor, (int) wrapDivisor);
        checkObjects(deltaTime);
        batch.end();

    }

    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    public void pause() {
    }

    public void resume() {
    }


    private void handleInput() {

        float cameraSpeed = camera.zoom * 2048;

        //System.out.println("Zoom: "+camera.zoom);

        // Rotate the sprite with left arrow key
        if (InputManager.isAPressed()) {
            for (Ship ship : playerShips) {
                ship.rotate(+0.3f);
            }
        }

        // Rotate the sprite with right arrow key
        if (InputManager.isDPressed()) {
            for (Ship ship : playerShips) {
                ship.rotate(-0.3f);
            }
        }

        // Shoot lasers
        if (InputManager.isSpacePressed()) {
            if (!laserSpawnTimeout) {
                for (Ship ship : playerShips) {

                    Laser laser = ship.fireLaser(blueLaserTexture, ship);
                    laser.setShip(ship);
                    lasers.add(laser);

                    laserBlast2.play(2.0f);
                    laserSpawnTimeout = true;
                    laserSpawnCounter = 0;


                }
            }
        }
//attack player
        if (InputManager.isRightMousePressed()) {
            for (CpuShip cpuShip : cpuShips) {

                cpuShip.setActionState(Ship.ActionState.ATTACK, cpuShip.getActionState());

            }

        }

        if (shipSpawnTimeout) {
            if (shipSpawnCounter >= 90) {

                shipSpawnTimeout = false;
            } else {
                shipSpawnCounter++;
            }
        }
// For player ship only during runtime. CpuShip laser timing is handled differently.
        if (laserSpawnTimeout) {
            if (laserSpawnCounter >= 200) {

                laserSpawnTimeout = false;
            } else {
                laserSpawnCounter++;
            }
        }

        int actionCounter = 0;

        if (InputManager.isLeftMousePressed()) {
            if (!shipSpawnTimeout) {
                Vector2 position = new Vector2(camera.position.x, camera.position.y);
                CpuShip.ActionState actionState = Ship.ActionState.IDLE;
                Rectangle hitBox = new Rectangle();
                ObjectSet<Ship> targets = new ObjectSet<>();
                CpuShip cpuShip = new CpuShip(greenShipTexture, position, 400f, actionState, Ship.ActionState.IDLE, hitBox, actionCounter, Ship.Faction.TEAL, targets);
                cpuShip.setPosition(position.x, position.y);
                cpuShip.setScale(shipScale);
                cpuShips.add(cpuShip);
                copiedSet.add(cpuShip);


                shipSpawnTimeout = true;
                shipSpawnCounter = 0;
            }
        }


        float speedLimit = 500f;
        // Speed up.
        if (InputManager.isWPressed()) {

            for (PlayerShip playerShip : playerShips) {

                if (playerShip.getSpeed() < speedLimit && playerShip.getSpeed() >= 0) {
                    playerShip.setSpeed(playerShip.getSpeed() + 1);
                }

            }
        }

        // Slow down.
        if (InputManager.isSPressed()) {
            for (PlayerShip playerShip : playerShips) {
                if (playerShip.getSpeed() <= speedLimit && playerShip.getSpeed() > 0) {
                    playerShip.setSpeed(playerShip.getSpeed() - 1);
                }
            }
        }

        if (InputManager.isEscPressed()) {
            Gdx.app.exit();
        }

        if (InputManager.isLeftPressed()) {
            camera.translate(-cameraSpeed * Gdx.graphics.getDeltaTime(), 0);
        }
        if (InputManager.isRightPressed()) {
            camera.translate(cameraSpeed * Gdx.graphics.getDeltaTime(), 0);
        }
        if (InputManager.isUpPressed()) {
            camera.translate(0, cameraSpeed * Gdx.graphics.getDeltaTime());
        }
        if (InputManager.isDownPressed()) {
            camera.translate(0, -cameraSpeed * Gdx.graphics.getDeltaTime());

            if (!shipSpawnTimeout) {
                Vector2 position = new Vector2(camera.position.x, camera.position.y);
                CpuShip.ActionState actionState = Ship.ActionState.IDLE;
                Rectangle hitBox = new Rectangle();
                ObjectSet<Ship> targets = new ObjectSet<>();
                CpuShip cpuShip = new CpuShip(otherShipTexture, position, 400f, actionState, Ship.ActionState.IDLE, hitBox, actionCounter, Ship.Faction.PURPLE, targets);
                cpuShip.setPosition(position.x, position.y);
                cpuShip.setScale(shipScale);
                cpuShips.add(cpuShip);


                shipSpawnTimeout = true;
                shipSpawnCounter = 0;
            }

        }

        if (InputManager.isQPressed()) {
            zoomIn();
        }

        if (InputManager.isEPressed()) {
            zoomOut();
        }

    }

    /////////////////////////////////
    private void zoomIn() {
        camera.zoom -= zoomSpeed * camera.zoom;
        camera.update();
    }

    // Method to zoom out
    private void zoomOut() {
        camera.zoom += zoomSpeed * camera.zoom;
        camera.update();
    }

    /////////////////////////////////
    public void checkIterators(Iterator<PlayerShip> playerIter, Iterator<Explosion> explosionIter, Iterator<CpuShip> cpuIter, Iterator<CpuShip> copyIter, Iterator<Laser> laserIter, float deltaTime) {
        while (playerIter.hasNext()) {

            PlayerShip playerShip = playerIter.next();
            playerShip.update(deltaTime, playerShip, WORLD_WIDTH, WORLD_HEIGHT);

            if (!playerShip.isActive()) {
                playerIter.remove();
            }
        }

        while (cpuIter.hasNext()) {

            CpuShip cpuShip = cpuIter.next();
            cpuShip.update(deltaTime, cpuShip, WORLD_WIDTH, WORLD_HEIGHT);

            if (!cpuShip.isActive()) {
                cpuIter.remove();
            }
        }

        while (copyIter.hasNext()) {

            CpuShip cpuShip = copyIter.next();
            cpuShip.update(deltaTime, cpuShip, WORLD_WIDTH, WORLD_HEIGHT);

            if (!cpuShip.isActive()) {
                copyIter.remove();

            }
        }

        while (explosionIter.hasNext()) {

            Explosion explosion = explosionIter.next();
            explosion.update(deltaTime);

            if (!explosion.isActive()) {
                explosionIter.remove();
            }
        }

        while (laserIter.hasNext()) {

            Laser laser = laserIter.next();
            laser.update(deltaTime, WORLD_WIDTH, WORLD_HEIGHT, laser.getDespawnCounter(), laser.getShip());

            if (!laser.isActive()) {
                laserIter.remove();
            }
        }

    }


    public void checkObjects(float deltaTime) {
        for (Laser laser : lasers) {
            laser.setScale(3);
            laser.draw(batch);
            Rectangle laserHitBox = laser.getHitbox();
            laser.update(deltaTime, WORLD_WIDTH, WORLD_HEIGHT, laser.getDespawnCounter(), laser.getShip());
            laser.updateHitBox(laser);

            for (CpuShip cpuShip : cpuShips) {
                Rectangle shipHitBox = cpuShip.getHitbox();

                if (laserHitBox.overlaps(shipHitBox) && laser.getShip().getFaction() != cpuShip.getFaction()) {
                    Vector2 position = new Vector2(laser.getX(), laser.getY() - 64);
                    Explosion.explode(camera, explosionTexture1, 0.7f, position, 30, explosions, explosionSound1, 300, 10);
                    cpuShip.setInactive(cpuShip);
                    laser.setInactive(laser);

                    if (showHitBoxes) {
                        cpuShip.getShapeRenderer().setProjectionMatrix(camera.combined);
                        cpuShip.drawBoundingBox();
                    }
                }
               // cpuShip.setActionState(Ship.ActionState.ATTACK, cpuShip.getActionState());

            }


            for (PlayerShip playerShip : playerShips) {
                Rectangle shipHitBox = playerShip.getHitbox();

                if (laserHitBox.overlaps(shipHitBox) && laser.getShip().getFaction() != playerShip.getFaction()) {
                    Vector2 position = new Vector2(laser.getX(), laser.getY() - 64);
                    Explosion.explode(camera, explosionTexture1, 0.7f, position, 30, explosions, explosionSound1, 300, 10);
                    playerShip.setInactive(playerShip);
                    laser.setInactive(laser);

                    if (showHitBoxes) {
                        playerShip.getShapeRenderer().setProjectionMatrix(camera.combined);
                        playerShip.drawBoundingBox();
                    }
                }
            }

        }


        for (PlayerShip playerShip : playerShips) {
            playerShip.draw(batch);
            playerShip.update(deltaTime, playerShip, WORLD_WIDTH, WORLD_HEIGHT);
            playerShip.handleActionState(playerShip, greenLaserTexture, blueLaserTexture, redLaserTexture, lasers, laserBlast2);
            camera.position.x = playerShip.getX() + 64;
            camera.position.y = playerShip.getY() + 64;
        }

        for (CpuShip cpuShip : cpuShips) {
            cpuShip.draw(batch);
            cpuShip.update(deltaTime, cpuShip, WORLD_WIDTH, WORLD_HEIGHT);
            cpuShip.handleActionState(cpuShip, greenLaserTexture, blueLaserTexture, redLaserTexture, lasers, laserBlast2);

            for (Ship target : cpuShip.getTargets()) {
                if (target != null) {
                    if (!target.isActive()) {
                        cpuShip.getTargets().remove(target);
                        System.out.println("TARGET REMOVED");
                    }
                }
            }

            for (PlayerShip playerShip : playerShips) {
                cpuShip.detectTargets(playerShip, cpuShip.getTargets());
            }

            for (CpuShip cpuShip2 : copiedSet) {
                cpuShip.detectTargets(cpuShip2, cpuShip.getTargets());
            }

        }





        for (Explosion explosion : explosions) {
            explosion.draw(batch);
            explosion.update(deltaTime);
        }

    }

    @Override
    public void dispose() {
        batch.dispose();
        backgroundTexture.dispose();
        purpleShipTexture.dispose();
        greenLaserTexture.dispose();
        blueLaserTexture.dispose();
        redLaserTexture.dispose();
        greenShipTexture.dispose();
        explosionSound1.dispose();
        laserBlast2.dispose();
        //   laserBlast1.dispose();
        explosionTexture1.dispose();
        otherShipTexture.dispose();
        exhaustTexture.dispose();

    }


}