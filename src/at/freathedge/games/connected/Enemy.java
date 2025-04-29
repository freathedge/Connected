package at.freathedge.games.connected;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.tiled.TiledMap;

public class Enemy {

    private float x, y;
    private float width, height;
    private float speed = 0.05f;
    private Player player;
    private GameMap map;
    private Image image;
    private Rectangle hitbox;

    private int attackCooldown = 3000; // in Millisekunden
    private int timeSinceLastAttack = 0;

    private int damage;
    private int damageRadius = 15;


    public Enemy(float x, float y, Player player, GameMap map, int damage) throws SlickException {
        this.x = x;
        this.y = y;
        this.player = player;
        this.map = map;
        this.width = 13;
        this.height = 20;
        this.image = new Image("res/enemy/enemy.png");
        this.hitbox = new Rectangle(x, y, width, height);
        this.damage = damage;
    }

    public void update(int delta) {
        float dx = player.getX() - x;
        float dy = player.getY() - y;

        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance > damageRadius - 5) {
            dx /= distance;
            dy /= distance;

            float newX = x + dx * speed * delta;
            float newY = y + dy * speed * delta;

            if (!checkCollision(newX, y)) {
                x = newX;
            }
            if (!checkCollision(x, newY)) {
                y = newY;
            }
        }

        hitbox.setX(x);
        hitbox.setY(y);

        timeSinceLastAttack += delta;


        float playerDistance = (float) Math.sqrt((player.getX() - x) * (player.getX() - x) + (player.getY() - y) * (player.getY() - y));
        if (playerDistance < damageRadius) {
            punch();
        }
    }

    private boolean checkCollision(float newX, float newY) {
        int tileX = (int) (newX / map.getTileWidth());
        int tileY = (int) (newY / map.getTileHeight());

        if (tileX < 0 || tileX >= map.getWidth() || tileY < 0 || tileY >= map.getHeight()) {
            return true;
        }

        int layer = map.getLayerIndex("wall");
        int tileID = map.getTileId(tileX, tileY, layer); // Layer 1 (Wall-Layer)
        return tileID != 0;
    }

    public void punch() {

        if (timeSinceLastAttack >= attackCooldown) {

            System.out.println("punch");
            player.damage(damage);
            timeSinceLastAttack = 0;
        }
    }

    public void render(Graphics g) {
        image.draw(x, y, width, height);
        g.setColor(Color.red);
        g.drawRect(x, y, width, height);
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
