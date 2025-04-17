package at.freathedge.games.connected;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.tiled.TiledMap;

public class Camera {
    private float x, y;
    private float targetX, targetY;
    private float lerpSpeed = 0.05f;
    private float offsetX = 100;
    private float offsetY = 50;

    private float zoomFactor = 1.0f;
    private float screenWidth, screenHeight;

    private TiledMap map;

    public Camera() {
        this.x = 0;
        this.y = 0;
        this.targetX = 0;
        this.targetY = 0;
    }

    public void setMap(TiledMap map) {
        this.map = map;
    }

    public void update(float playerX, float playerY, float screenWidth, float screenHeight, int delta) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        float zoomedHalfWidth = screenWidth / (2f * zoomFactor);
        float zoomedHalfHeight = screenHeight / (2f * zoomFactor);

        targetX = playerX - zoomedHalfWidth + offsetX;
        targetY = playerY - zoomedHalfHeight + offsetY;

        float maxX = map.getWidth() * map.getTileWidth() - screenWidth / zoomFactor;
        float maxY = map.getHeight() * map.getTileHeight() - screenHeight / zoomFactor;

        targetX = Math.max(0, Math.min(targetX, maxX));
        targetY = Math.max(0, Math.min(targetY, maxY));

        x += (targetX - x) * lerpSpeed * (delta / 16.0f);
        y += (targetY - y) * lerpSpeed * (delta / 16.0f);
    }

    public void apply(Graphics g) {
        g.scale(zoomFactor, zoomFactor);
        g.translate(-x, -y);
    }

    public void zoom(float factor) {
        this.zoomFactor = factor;
    }

    public float getZoom() {
        return zoomFactor;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
