package at.freathedge.games.connected;

import at.freathedge.games.connected.collider.Collider;
import at.freathedge.games.connected.util.AnimationLoader;
import at.freathedge.games.connected.util.SoundBank;
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
    private Direction punchDirection;

    private boolean moving = false;

    private float punchCirlceRadius = 25;
    private float punchCircleAngle = 150f;

    private boolean isPunching = false;

    private boolean recentlyDamaged = false;
    private long damageTimestamp = 0;
    private final int damageEffectDuration = 150;

    private long lastStepSoundTime = 0;


    private long lastDamageSoundTime = 0;

    private Map<Integer, Image> wallTileImages = new HashMap<>();

    private final Collider collider;

    private final int hitboxMarginX = 2;
    private final int hitboxHeight = 6;

    SoundBank grassSteps = new SoundBank();
    SoundBank stoneSteps = new SoundBank();
    SoundBank swingSounds = new SoundBank();
    SoundBank damageSounds = new SoundBank();


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
        loadWallTileImages();
        setDirection(currentDirection);
        this.punchDirection = currentDirection;
        this.collider = new Collider(map);

    }

    public void render(Graphics g, float mouseWorldX, float mouseWorldY) {

        g.setColor(Color.blue);
        Rectangle hitbox = getHitbox();
        g.drawRect(hitbox.getX(), hitbox.getY(), hitbox.getWidth(), hitbox.getHeight());

        collider.renderDebug(g);

        // Zeichne Spieler
        if (recentlyDamaged && System.currentTimeMillis() - damageTimestamp <= damageEffectDuration) {
            currentAnimation.getCurrentFrame().draw(x, y, width, height, new Color(255, 0, 0));
        } else {
            currentAnimation.getCurrentFrame().draw(x, y, width, height);
        }

        float centerX = x + width / 2f;
        float centerY = y + height / 2f;
        float dx = mouseWorldX - centerX;
        float dy = mouseWorldY - centerY;

        // Winkel berechnen (atan2: 0 = Ost, CCW positiv)
        double theta = Math.toDegrees(Math.atan2(dy, dx));
        // Für Slick: 0° = Süden, im Uhrzeigersinn steigend
        float startAngle = (float) ((theta + 270) % 360);

        if (isPunching) {
            g.setColor(new Color(255, 0, 0, 100));
            // gefüllter Halbkreis
            g.fillArc(
                    centerX - punchCirlceRadius, centerY - punchCirlceRadius,
                    50, 50,
                    startAngle,
                    startAngle + punchCircleAngle
            );
        } else {
            g.setColor(new Color(255, 255, 255));
            g.drawArc(
                    centerX - punchCirlceRadius, centerY - punchCirlceRadius,
                    50, 50,
                    startAngle,
                    startAngle + punchCircleAngle
            );
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
            switch (layerName) {
                case "floor.grass":
                    grassSteps.playRandom(1.0f, 0.3f);
                    break;
                case "floor.path":
                    stoneSteps.playRandom(1.0f, 0.3f);
                    break;
            }
            lastStepSoundTime = now;
        }
    }

    public void move(float delta, boolean up, boolean down, boolean left, boolean right) throws SlickException {
        float dx = 0, dy = 0;
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
                    float len = (float) Math.sqrt(dx * dx + dy * dy);
                    dx /= len;
                    dy /= len;
                }
                if (dy < 0) setDirection(Direction.BACK);
                else if (dy > 0) setDirection(Direction.FRONT);
                else if (dx < 0) setDirection(Direction.LEFT);
                else setDirection(Direction.RIGHT);
            }
        }

        float currentSpeed = speed;
        int tileSize = map.getTileWidth();
        int grassLayer = map.getLayerIndex("floor.grass");
        if (grassLayer != -1) {
            int bx = (int) (x / tileSize);
            int by = (int) ((y + height - 1) / tileSize);
            int leftTile = map.getTileId(bx, by, grassLayer);
            int rightTile = map.getTileId((int) ((x + width - 1) / tileSize), by, grassLayer);
            if (leftTile != 0 || rightTile != 0) {
                currentSpeed *= 0.6f;
            }
        }

        float step = currentSpeed * delta;
        float nx = x + dx * step;
        float ny = y + dy * step;

        Rectangle hitbox = getHitbox();

        Rectangle futureHitboxX = new Rectangle(
                hitbox.getX() + (nx - x),
                hitbox.getY(),
                hitbox.getWidth(),
                hitbox.getHeight()
        );
        if (!collider.isBlockedRectangle(futureHitboxX.getX(), futureHitboxX.getY(), futureHitboxX.getWidth(), futureHitboxX.getHeight())) {
            x = nx;
        }

        Rectangle futureHitboxY = new Rectangle(
                hitbox.getX(),
                hitbox.getY() + (ny - y),
                hitbox.getWidth(),
                hitbox.getHeight()
        );
        if (!collider.isBlockedRectangle(futureHitboxY.getX(), futureHitboxY.getY(), futureHitboxY.getWidth(), futureHitboxY.getHeight())) {
            y = ny;
        }
    }


    public void punch(boolean leftClick, float mouseWorldX, float mouseWorldY) {
        if (leftClick && !isPunching) {
            float centerX = x + width / 2f;
            float centerY = y + height / 2f;
            float dx = mouseWorldX - centerX;
            float dy = mouseWorldY - centerY;

            Direction punchDir;
            if (Math.abs(dx) > Math.abs(dy)) {
                punchDir = dx > 0 ? Direction.RIGHT : Direction.LEFT;
            } else {
                punchDir = dy > 0 ? Direction.FRONT : Direction.BACK;
            }

            punchDirection = punchDir;
            setDirection(punchDir);

            isPunching = true;
            swingSounds.stopAll();
            swingSounds.playRandom(1.0f, 1.5f);

            switch (punchDir) {
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

        int frameDuration = 100;
        idleAnimation = AnimationLoader.loadAnimation("res/player/idle/player_idle", 6, frameDuration);
        walkFrontAnimation = AnimationLoader.loadAnimation("res/player/walk_front/player_walking_front", 6, frameDuration);
        walkLeftAnimation = AnimationLoader.loadAnimation("res/player/walk_left/player_walking_left", 6, frameDuration);
        walkRightAnimation = AnimationLoader.loadAnimation("res/player/walk_right/player_walking_right", 6, frameDuration);
        walkBackAnimation = AnimationLoader.loadAnimation("res/player/walk_back/player_walking_back", 6, frameDuration);

        punchFrontAnimation = AnimationLoader.loadAnimation("res/player/swing_front/swing_front", 4, frameDuration);
        punchLeftAnimation = AnimationLoader.loadAnimation("res/player/swing_left/swing_left", 4, frameDuration);
        punchRightAnimation = AnimationLoader.loadAnimation("res/player/swing_right/swing_right", 4, frameDuration);
        punchBackAnimation = AnimationLoader.loadAnimation("res/player/swing_back/swing_back", 4, frameDuration);

        currentAnimation = idleAnimation;
    }

    private void loadSounds() throws SlickException {
        loadStepSounds(grassSteps, "res/sounds/player/grass_step/grass_step");
        loadStepSounds(stoneSteps, "res/sounds/player/stone_step/stone_step");

        swingSounds.addSound("res/sounds/player/sword_swing.ogg");
        damageSounds.addSound("res/sounds/player/player_damage.ogg");


    }

    private void loadStepSounds(SoundBank soundBank, String path) throws SlickException {
        for (int i = 0; i < 6; i++) {
            soundBank.addSound(path + (i + 1) + ".ogg");
        }
    }

    public void setDirection(Direction direction) {
        this.currentDirection = direction;
        updateAnimation();
    }

    private void updateAnimation() {
        if (isPunching) return;

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


    private String getCurrentFootstepLayer() {
        int tileX = (int) ((x + width / 2f) / map.getTileWidth());
        int tileY = (int) ((y + height) / map.getTileHeight());

        int grassLayer = map.getLayerIndex("floor.grass");
        int pathLayer = map.getLayerIndex("floor.path");

        boolean onGrass = false;
        boolean onPath = false;

        for (int i = 0; i < map.getLayerCount(); i++) {
            int tileId = map.getTileId(tileX, tileY, i);

            if (tileId != 0) {
                if (i == grassLayer) {
                    onGrass = true;
                }
                if (i == pathLayer) {
                    onPath = true;
                }
            }
        }

        if (onPath) {
            System.out.println("floor.path");
            return "floor.path";
        }
        if (onGrass) {
            System.out.println("floor.grass");
            return "floor.grass";
        }

        // Standardfallback
        return "floor.grass";
    }


    public void damage(int damage) {
        this.health -= damage;
        recentlyDamaged = true;
        damageTimestamp = System.currentTimeMillis();

        long now = damageTimestamp;
        // in Millisekunden
        long damageSoundCooldown = 200;
        if (now - lastDamageSoundTime >= damageSoundCooldown) {
            damageSounds.stopAll();
            damageSounds.playRandom(1.0f, 0.5f);
            lastDamageSoundTime = now;
        }

        checkDeath();
    }

    public void checkDeath() {
        if (health <= 0) {
            // TODO: Handle player death (e.g., restart level, show game over, etc.)
        }
    }

    // Getters & Setters

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int maxHealth() {
        return maxHealth;
    }

    public int health() {
        return health;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    private void loadWallTileImages() throws SlickException {
        int wallLayer = map.getLayerIndex("wall");
        if (wallLayer == -1) return;

        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                int tileId = map.getTileId(x, y, wallLayer);
                if (tileId != 0 && !wallTileImages.containsKey(tileId)) {
                    Image tileImage = map.getTileImage(x, y, wallLayer);
                    if (tileImage != null) {
                        wallTileImages.put(tileId, tileImage);
                    }
                }
            }
        }
    }

    public Rectangle getHitbox() {
        return new Rectangle(
                x + hitboxMarginX,
                y + height - hitboxHeight,
                width - hitboxMarginX * 2,
                hitboxHeight
        );
    }


}
