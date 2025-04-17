package at.freathedge.games.connected;

import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Rectangle;

public class Tile {

    private final int x, y, size;
    private final boolean solid;
    private final Image sprite;

    public Tile(int x, int y, int size, boolean solid, Image sprite) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.solid = solid;
        this.sprite = sprite;
    }

    public void render(org.newdawn.slick.Graphics g, float cameraX, float cameraY) {
        sprite.draw(x - cameraX, y - cameraY);
    }

    public boolean isSolid() {
        return solid;
    }

    public Rectangle getHitbox() {
        return new Rectangle(x, y, size, size);
    }
}
