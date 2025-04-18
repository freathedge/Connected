package at.freathedge.games.connected;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.tiled.TiledMap;

import java.util.HashMap;
import java.util.Map;

public class Player {

    private float x, y;
    private final int width = 13;
    private final int height = 20;
    private final float speed = 0.1f;

    private final int maxHealth = 100;
    private int health = 100;

    private final TiledMap map;

    private Animation idleAnimation;
    private Animation walkFrontAnimation;
    private Animation walkLeftAnimation;
    private Animation walkRightAnimation;
    private Animation walkBackAnimation;
    private Animation currentAnimation;

    private Animation punchFrontAnimation;
    private Animation punchLeftAnimation;
    private Animation punchRightAnimation;
    private Animation punchBackAnimation;

    private Direction currentDirection;
    private boolean moving = false;

    private boolean punchCircleActive = false;
    private long punchCircleStartTime = 0;
    private final int punchCircleDuration = 400;

    private boolean isPunching = false;

    private boolean recentlyDamaged = false;
    private long damageTimestamp = 0;
    private final int damageEffectDuration = 150; // in Millisekunden



    private Map<String, Sound[]> stepSoundsByLayer = new HashMap<>();
    private long lastStepSoundTime = 0;
    private int stepSoundIndex = 0;

    private Sound swingSound;
    private Sound damageSound;

    private long lastDamageSoundTime = 0;
    private final long damageSoundCooldown = 200; // in Millisekunden

    public enum Direction {
        FRONT, BACK, LEFT, RIGHT
    }

    public Player(float x, float y, TiledMap map) throws SlickException {
        this.x = x;
        this.y = y;
        this.map = map;
        this.currentDirection = Direction.FRONT;
        loadAnimations();
        loadSounds();
        setDirection(currentDirection);
    }

    public void render(Graphics g) {
        long now = System.currentTimeMillis();

        Image frame = currentAnimation.getCurrentFrame();

        if (recentlyDamaged && now - damageTimestamp <= damageEffectDuration) {
            frame.draw(x, y, width, height, new Color(255, 0, 0));
        } else {
            recentlyDamaged = false;
            frame.draw(x, y, width, height);
        }


        if (punchCircleActive) {
            long elapsed = System.currentTimeMillis() - punchCircleStartTime;
            if (elapsed <= punchCircleDuration) {
                float centerX = x + width / 2f;
                float centerY = y + height / 2f;
                float radius = 25f;

                g.setColor(new Color(255, 0, 0, 100));

                float startAngle = 0f;
                switch (currentDirection) {
                    case FRONT: // unten
                        startAngle = 0f;
                        break;
                    case RIGHT:
                        startAngle = 270f;
                        break;
                    case BACK: // oben
                        startAngle = 180f;
                        break;
                    case LEFT:
                        startAngle = 90f;
                        break;
                }

                g.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, startAngle, startAngle + 180f);
            } else {
                punchCircleActive = false;
            }
        }

    }

    public void update(int delta) {
        if (isPunching) {
            currentAnimation.update(delta);

            if (currentAnimation.getFrame() == currentAnimation.getFrameCount() - 1) {
                isPunching = false;
                updateAnimation();
            }
        } else {
            currentAnimation.update(delta);
        }


        long now = System.currentTimeMillis();
        if (moving && now - lastStepSoundTime >= 500) {
            String layerName = getCurrentFootstepLayer();

            Sound[] sounds = stepSoundsByLayer.getOrDefault(layerName, stepSoundsByLayer.get("floor.grass"));
            if (sounds != null && sounds.length > 0) {
                sounds[stepSoundIndex].play(1.0f, 0.3f);
                stepSoundIndex = (stepSoundIndex + 1) % sounds.length;
            }
            lastStepSoundTime = now;
        }
    }

    public void move(float delta, boolean up, boolean down, boolean left, boolean right) throws SlickException {
        float dx = 0;
        float dy = 0;

        if (left) dx -= 1;
        else if (right) dx += 1;

        if (up) dy -= 1;
        else if (down) dy += 1;

        moving = (dx != 0 || dy != 0);

        if (!isPunching) {
            if (!moving) {
                setDirection(Direction.FRONT);
            } else {
                if (dx != 0 && dy != 0) {
                    float length = (float) Math.sqrt(dx * dx + dy * dy);
                    dx /= length;
                    dy /= length;
                }

                if (dy < 0) setDirection(Direction.BACK);
                else if (dy > 0) setDirection(Direction.FRONT);
                else if (dx < 0) setDirection(Direction.LEFT);
                else setDirection(Direction.RIGHT);
            }
        }

        float currentSpeed = speed;
        int tileSize = map.getTileWidth();
        int floorLayerIndex = map.getLayerIndex("floor.grass");
        if (floorLayerIndex != -1) {
            int bottomLeftX = (int)(x / tileSize);
            int bottomLeftY = (int)((y + height - 1) / tileSize);
            int bottomRightX = (int)((x + width - 1) / tileSize);
            int bottomRightY = bottomLeftY;
            int leftTileID = map.getTileId(bottomLeftX, bottomLeftY, floorLayerIndex);
            int rightTileID = map.getTileId(bottomRightX, bottomRightY, floorLayerIndex);
            if (leftTileID != 0 || rightTileID != 0) {
                currentSpeed *= 0.6f;
            }
        }

        float playerSpeed = currentSpeed * delta;
        float nextX = x + dx * playerSpeed;
        float nextY = y + dy * playerSpeed;

        if (!isBlocked(nextX, y)) x = nextX;
        if (!isBlocked(x, nextY)) y = nextY;
    }

    public void punch(boolean leftClick) {

        if (leftClick && !isPunching) {

            punchCircleActive = true;
            punchCircleStartTime = System.currentTimeMillis();
            isPunching = true;
            swingSound.stop();
            swingSound.play(1.0f, 1.5f);

            switch (currentDirection) {
                case FRONT:
                    currentAnimation = punchFrontAnimation;
                    break;
                case LEFT:
                    currentAnimation = punchLeftAnimation;
                    break;
                case RIGHT:
                    currentAnimation = punchRightAnimation;
                    break;
                case BACK:
                    currentAnimation = punchBackAnimation;
                    break;
            }
        }
    }

    private void loadAnimations() throws SlickException {
        Image[] idleFrames = new Image[6];
        Image[] walkFrontFrames = new Image[6];
        Image[] walkLeftFrames = new Image[6];
        Image[] walkRightFrames = new Image[6];
        Image[] walkBackFrames = new Image[6];

        Image[] punchFrontFrames = new Image[4];
        Image[] punchLeftFrames = new Image[4];
        Image[] punchRightFrames = new Image[4];
        Image[] punchBackFrames = new Image[4];

        for (int i = 0; i < 6; i++) {
            idleFrames[i] = new Image("res/player/idle/player_idle" + (i + 1) + ".png");
            walkFrontFrames[i] = new Image("res/player/walk_front/player_walking_front" + (i + 1) + ".png");
            walkLeftFrames[i] = new Image("res/player/walk_left/player_walking_left" + (i + 1) + ".png");
            walkRightFrames[i] = new Image("res/player/walk_right/player_walking_right" + (i + 1) + ".png");
            walkBackFrames[i] = new Image("res/player/walk_back/player_walking_back" + (i + 1) + ".png");
        }

        for (int i = 0; i < 4; i++) {
            punchFrontFrames[i] = new Image("res/player/swing_front/swing_front" + (i + 1) + ".png");
            punchLeftFrames[i] = new Image("res/player/swing_left/swing_left" + (i + 1) + ".png");
            punchRightFrames[i] = new Image("res/player/swing_right/swing_right" + (i + 1) + ".png");
            punchBackFrames[i] = new Image("res/player/swing_back/swing_back" + (i + 1) + ".png");
        }

        int duration = 100;

        idleAnimation = new Animation(idleFrames, duration);
        walkFrontAnimation = new Animation(walkFrontFrames, duration);
        walkLeftAnimation = new Animation(walkLeftFrames, duration);
        walkRightAnimation = new Animation(walkRightFrames, duration);
        walkBackAnimation = new Animation(walkBackFrames, duration);

        punchFrontAnimation = new Animation(punchFrontFrames, duration);
        punchLeftAnimation = new Animation(punchLeftFrames, duration);
        punchRightAnimation = new Animation(punchRightFrames, duration);
        punchBackAnimation = new Animation(punchBackFrames, duration);

        currentAnimation = idleAnimation;
    }

    private void loadSounds() throws SlickException {
        stepSoundsByLayer.put("floor.grass", loadStepSoundsFromPath("res/sounds/player/grass_step/grass_step"));
        stepSoundsByLayer.put("floor.path", loadStepSoundsFromPath("res/sounds/player/stone_step/stone_step"));

        swingSound = new Sound("res/sounds/player/sword_swing.ogg");

        damageSound = new Sound("res/sounds/player/player_damage.ogg");
    }

    private Sound[] loadStepSoundsFromPath(String basePath) throws SlickException {
        Sound[] sounds = new Sound[6];
        for (int i = 0; i < 6; i++) {
            sounds[i] = new Sound(basePath + (i + 1) + ".ogg");
        }
        return sounds;
    }

    public void setDirection(Direction direction) {
        this.currentDirection = direction;
        updateAnimation();
    }

    private void updateAnimation() {
        if(isPunching) {
            return;
        }
        if (moving) {
            switch (currentDirection) {
                case FRONT:
                    currentAnimation = walkFrontAnimation;
                    break;
                case LEFT:
                    currentAnimation = walkLeftAnimation;
                    break;
                case RIGHT:
                    currentAnimation = walkRightAnimation;
                    break;
                case BACK:
                    currentAnimation = walkBackAnimation;
                    break;
            }
        } else {
            currentAnimation = idleAnimation;
        }
    }

    private boolean isBlocked(float x, float y) {
        int tileSize = map.getTileWidth();
        float tolerance = 1f;

        int left = (int)((x + tolerance) / tileSize);
        int right = (int)((x + width - 1 - tolerance) / tileSize);
        int top = (int)((y + tolerance) / tileSize);
        int bottom = (int)((y + height - 1 - tolerance) / tileSize);

        int wallLayerIndex = map.getLayerIndex("wall");
        if (wallLayerIndex == -1) return false;

        for (int i = left; i <= right; i++) {
            if (map.getTileId(i, top, wallLayerIndex) != 0) return true;
            if (map.getTileId(i, bottom, wallLayerIndex) != 0) return true;
        }
        for (int i = top; i <= bottom; i++) {
            if (map.getTileId(left, i, wallLayerIndex) != 0) return true;
            if (map.getTileId(right, i, wallLayerIndex) != 0) return true;
        }

        return false;
    }

    private String getCurrentFootstepLayer() {
        int tileX = (int)((x + width / 2) / map.getTileWidth());
        int tileY = (int)((y + height) / map.getTileHeight());

        int grassLayer = map.getLayerIndex("floor.grass");
        int pathLayer = map.getLayerIndex("floor.path");

        for (int i = 0; i < map.getLayerCount(); i++) {
            int tileId = map.getTileId(tileX, tileY, i);

            if (tileId != 0) {
                if (i == grassLayer) {
                    return "floor.grass";
                } else if (i == pathLayer) {
                    return "floor.path";
                }

            }
        }

        return "";
    }

    public void damage(int damage) {
        this.health -= damage;
        recentlyDamaged = true;
        damageTimestamp = System.currentTimeMillis();

        long now = System.currentTimeMillis();
        if (now - lastDamageSoundTime >= damageSoundCooldown) {
            damageSound.stop();
            damageSound.play(1.0f, 0.5f);
            lastDamageSoundTime = now;
        }

        checkDeath();
    }

    public void checkDeath() {
        if(health <= 0) {

        }
    }



    public float getX() { return x; }
    public float getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public int maxHealth() {
        return maxHealth;
    }

    public int health() {
        return health;
    }

    public void setPosition(float x, float y) { this.x = x; this.y = y; }
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
}
