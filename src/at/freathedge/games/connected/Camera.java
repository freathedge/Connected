package at.freathedge.games.connected;

import org.newdawn.slick.Graphics;

public class Camera {
    private float x, y;
    private float targetX, targetY;
    private float lerpSpeed = 0.05f;
    private float offsetX = 100;
    private float offsetY = 50;

    public Camera() {
        this.x = 0;
        this.y = 0;
        this.targetX = 0;
        this.targetY = 0;
    }

    public void update(float playerX, float playerY, float screenWidth, float screenHeight, int delta) {
        targetX = playerX - screenWidth / 2 + offsetX;
        targetY = playerY - screenHeight / 2 + offsetY;

        x += (targetX - x) * lerpSpeed * (delta / 16.0f);
        y += (targetY - y) * lerpSpeed * (delta / 16.0f);
    }

    public void apply(Graphics g) {
        g.translate(-x, -y);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
