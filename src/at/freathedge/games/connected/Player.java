package at.freathedge.games.connected;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.tiled.TiledMap;

public class Player {

    private float x, y;
    private int width = 27;
    private int height = 35;
    private final float speed = 0.2f;

    private final TiledMap map;
    private Image front, back, left, right;
    private Image currentImage;
    private Direction currentDirection;

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
        loadImages();
        setDirection(currentDirection);
    }

    public void render(Graphics g) {
        g.drawImage(currentImage, x, y);
        g.setColor(Color.green);
        g.setLineWidth(3);
        g.drawRect(x, y, width, height);

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

    private void loadImages() throws SlickException {
        front = new Image("res/player/front.png");
        back = new Image("res/player/back.png");
        left = new Image("res/player/left.png");
        right = new Image("res/player/right.png");
        currentImage = front;
    }

    public void setDirection(Direction direction) throws SlickException {
        this.currentDirection = direction;
        switch (direction) {
            case FRONT:
                currentImage = front;
                break;
            case BACK:
                currentImage = back;
                break;
            case LEFT:
                currentImage = left;
                break;
            case RIGHT:
                currentImage = right;
                break;
        }
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

    public void move(float delta, boolean up, boolean down, boolean left, boolean right) throws SlickException {
        float dx = 0;
        float dy = 0;

        if (left) {
            dx -= 1;
        } else if (right) {
            dx += 1;
        }

        if (up) {
            dy -= 1;
        } else if (down) {
            dy += 1;
        }

        if (!up && !down && !left && !right) {
            setDirection(Direction.FRONT);
        } else {
            if (dx != 0 && dy != 0) {
                float length = (float) Math.sqrt(dx * dx + dy * dy);
                dx /= length;
                dy /= length;
            }

            if (dy < 0) {
                setDirection(Direction.BACK);
            } else if (dy > 0) {
                setDirection(Direction.FRONT);
            } else if (dx < 0) {
                setDirection(Direction.LEFT);
            } else {
                setDirection(Direction.RIGHT);
            }
        }

        float currentSpeed = speed;

        int tileSize = map.getTileWidth();
        int floorLayerIndex = map.getLayerIndex("Floor");
        if (floorLayerIndex != -1) {
            // Untere linke Ecke
            int bottomLeftX = (int)(x / tileSize);
            int bottomLeftY = (int)((y + height - 1) / tileSize);

            // Untere rechte Ecke
            int bottomRightX = (int)((x + width - 1) / tileSize);
            int bottomRightY = bottomLeftY;

            // Tile-IDs der unteren linken und rechten Ecke
            int leftTileID = map.getTileId(bottomLeftX, bottomLeftY, floorLayerIndex);
            int rightTileID = map.getTileId(bottomRightX, bottomRightY, floorLayerIndex);

            // Wenn eine der Ecken auf einem "Floor"-Layer steht, dann verlangsame die Bewegung
            if (leftTileID != 0 || rightTileID != 0) {
                currentSpeed *= 0.6f; // Verlangsamung auf "Floor"-Layer
            }
        }

        float playerSpeed = currentSpeed * delta;
        float nextX = x + dx * playerSpeed;
        float nextY = y + dy * playerSpeed;

        if (!isBlocked(nextX, y)) {
            x = nextX;
        }

        if (!isBlocked(x, nextY)) {
            y = nextY;
        }
    }



    private boolean isBlocked(float x, float y) {
        int tileSize = map.getTileWidth();

        // Toleranz einbauen, damit obere Kante nicht zu hoch erkannt wird
        float tolerance = 1f;

        // Kanten des Spielers
        int left = (int)((x + tolerance) / tileSize);
        int right = (int)((x + width - 1 - tolerance) / tileSize);
        int top = (int)((y + tolerance) / tileSize);  // <- hier der Fix!
        int bottom = (int)((y + height - 1 - tolerance) / tileSize);

        int wallLayerIndex = map.getLayerIndex("wall");
        if (wallLayerIndex == -1) return false;

        // obere Kante prüfen
        for (int i = left; i <= right; i++) {
            if (map.getTileId(i, top, wallLayerIndex) != 0) {
                return true;
            }
        }

        // untere Kante prüfen
        for (int i = left; i <= right; i++) {
            if (map.getTileId(i, bottom, wallLayerIndex) != 0) {
                return true;
            }
        }

        // linke Kante prüfen
        for (int i = top; i <= bottom; i++) {
            if (map.getTileId(left, i, wallLayerIndex) != 0) {
                return true;
            }
        }

        // rechte Kante prüfen
        for (int i = top; i <= bottom; i++) {
            if (map.getTileId(right, i, wallLayerIndex) != 0) {
                return true;
            }
        }

        return false;
    }








    private int[][] getCheckPoints(float x, float y, int tileSize) {
        return new int[][]{
                { (int)(x / tileSize),                 (int)(y / tileSize) },
                { (int)((x + this.width - 1) / tileSize), (int)(y / tileSize) },
                { (int)(x / tileSize),                 (int)((y + this.height - 1) / tileSize) },
                { (int)((x + this.width - 1) / tileSize), (int)((y + this.height - 1) / tileSize) }
        };
    }

    public void punch(boolean leftClick) {
        if (leftClick) {
            System.out.println("leftClick");
            punchCircleActive = true;
            punchCircleStartTime = System.currentTimeMillis();
        }
    }
}
