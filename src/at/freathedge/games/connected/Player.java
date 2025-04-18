package at.freathedge.games.connected;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.tiled.TiledMap;

public class Player {

    private float x, y;
    private final int width = 13;
    private final int height = 20;
    private final float speed = 0.2f;

    private final TiledMap map;

    private Animation idleAnimation;
    private Animation walkFrontAnimation;
    private Animation walkLeftAnimation;
    private Animation walkRightAnimation;
    private Animation walkBackAnimation;
    private Animation currentAnimation;

    private Direction currentDirection;
    private boolean moving = false;

    private boolean punchCircleActive = false;
    private long punchCircleStartTime = 0;
    private final int punchCircleDuration = 1000;

    public enum Direction {
        FRONT, BACK, LEFT, RIGHT
    }

    public Player(float x, float y, TiledMap map) throws SlickException {
        this.x = x;
        this.y = y;
        this.map = map;
        this.currentDirection = Direction.FRONT;
        loadAnimations();
        setDirection(currentDirection);
    }

    public void render(Graphics g) {
        currentAnimation.getCurrentFrame().draw(x, y, width, height);

        // Debug hitbox
//        g.setColor(Color.green);
//        g.setLineWidth(3);
//        g.drawRect(x, y, width, height);

        if (punchCircleActive) {
            long elapsed = System.currentTimeMillis() - punchCircleStartTime;
            if (elapsed <= punchCircleDuration) {
                float centerX = x + width / 2f;
                float centerY = y + height / 2f;
                float radius = 75f;

                g.setColor(new Color(255, 0, 0, 100));
                g.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
            } else {
                punchCircleActive = false;
            }
        }
    }

    public void update(int delta) {
        currentAnimation.update(delta);
    }


    private void loadAnimations() throws SlickException {
        Image[] idleFrames = new Image[6];
        Image[] walkFrontFrames = new Image[6];
        Image[] walkLeftFrames = new Image[6];
        Image[] walkRightFrames = new Image[6];
        Image[] walkBackFrames = new Image[6];

        for (int i = 0; i < 6; i++) {
            idleFrames[i] = new Image("res/player/idle/player_idle" + (i + 1) + ".png");
            walkFrontFrames[i] = new Image("res/player/walk_front/player_walking_front" + (i + 1) + ".png");
            walkLeftFrames[i] = new Image("res/player/walk_left/player_walking_left" + (i + 1) + ".png");
            walkRightFrames[i] = new Image("res/player/walk_right/player_walking_right" + (i + 1) + ".png");
            walkBackFrames[i] = new Image("res/player/walk_back/player_walking_back" + (i + 1) + ".png");
        }

        int duration = 100; // ms per frame

        idleAnimation = new Animation(idleFrames, duration);
        walkFrontAnimation = new Animation(walkFrontFrames, duration);
        walkLeftAnimation = new Animation(walkLeftFrames, duration);
        walkRightAnimation = new Animation(walkRightFrames, duration);
        walkBackAnimation = new Animation(walkBackFrames, duration);

        currentAnimation = idleAnimation; // Default animation
    }

    public void setDirection(Direction direction) {
        this.currentDirection = direction;
        updateAnimation();
    }

    private void updateAnimation() {
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

    public void move(float delta, boolean up, boolean down, boolean left, boolean right) throws SlickException {
        float dx = 0;
        float dy = 0;

        if (left) dx -= 1;
        else if (right) dx += 1;

        if (up) dy -= 1;
        else if (down) dy += 1;

        moving = (dx != 0 || dy != 0);

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

        updateAnimation();

        float currentSpeed = speed;
        int tileSize = map.getTileWidth();
        int floorLayerIndex = map.getLayerIndex("Floor");
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

    public void punch(boolean leftClick) {
        if (leftClick) {
            punchCircleActive = true;
            punchCircleStartTime = System.currentTimeMillis();
        }
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public void setPosition(float x, float y) { this.x = x; this.y = y; }
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
}
