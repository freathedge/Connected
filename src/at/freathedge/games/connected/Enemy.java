package at.freathedge.games.connected;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.tiled.TiledMap;

public class Enemy {

    private float x, y;
    private float speed = 0.1f;
    private Player player;
    private TiledMap map;
    private Image image;
    private Rectangle hitbox;

    public Enemy(float x, float y, Player player, TiledMap map) throws SlickException {
        this.x = x;
        this.y = y;
        this.player = player;
        this.map = map;
        this.image = new Image("res/enemy/enemy.png");
        this.hitbox = new Rectangle(x, y, image.getWidth(), image.getHeight());
    }

    public void update(int delta) {
        float dx = player.getX() - x;
        float dy = player.getY() - y;

        // Berechne die Richtung
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance > 1) {
            // Normalisiere die Richtung
            dx /= distance;
            dy /= distance;

            // Berechne die neue Position
            float newX = x + dx * speed * delta;
            float newY = y + dy * speed * delta;

            // Überprüfe auf Kollision mit der Wand
            if (!checkCollision(newX, y)) {
                x = newX; // nur bewegen, wenn keine Kollision mit Wänden
            }
            if (!checkCollision(x, newY)) {
                y = newY; // nur bewegen, wenn keine Kollision mit Wänden
            }
        }

        // Update der Hitbox
        hitbox.setX(x);
        hitbox.setY(y);
    }

    private boolean checkCollision(float newX, float newY) {
        int tileX = (int) (newX / map.getTileWidth());
        int tileY = (int) (newY / map.getTileHeight());

        if (tileX < 0 || tileX >= map.getWidth() || tileY < 0 || tileY >= map.getHeight()) {
            return true;
        }

        int tileID = map.getTileId(tileX, tileY, 1); // Layer 1 (Wall-Layer)
        if (tileID != 0) {
            return true;
        }

        return false;
    }

    public void render(Graphics g) {
        image.draw(x, y);
        g.setColor(Color.red);
        g.drawRect(x, y, image.getWidth(), image.getHeight());
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
