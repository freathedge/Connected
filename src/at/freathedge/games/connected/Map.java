package at.freathedge.games.connected;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;
import java.util.ArrayList;
import java.util.List;

public abstract class Map {

    protected Tile[][] tiles;
    protected int tileSize = 50;
    protected int spawnX;
    protected int spawnY;
    protected Image wallTile, floorTile;

    public Map(String floorPath, String wallPath, int[][] layout, int spawnX, int spawnY) throws SlickException {
        floorTile = new Image(floorPath);
        wallTile = new Image(wallPath);
        this.spawnX = spawnX;
        this.spawnY = spawnY;

        tiles = new Tile[layout.length][layout[0].length];

        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[y].length; x++) {
                boolean isWall = layout[y][x] == 1;
                Image img = isWall ? wallTile : floorTile;
                tiles[y][x] = new Tile(x * tileSize, y * tileSize, tileSize, isWall, img);
            }
        }
    }

    public void render(Graphics g, Camera camera, GameContainer gc, int tileWidth, int tileHeight) {
        for (int y = 0; y < tiles.length; y++) {
            for (int x = 0; x < tiles[y].length; x++) {
                tiles[y][x].render(g, camera.getX(), camera.getY());
            }
        }
    }

    public List<Rectangle> getWallHitboxes() {
        List<Rectangle> walls = new ArrayList<>();
        for (Tile[] row : tiles) {
            for (Tile tile : row) {
                if (tile.isSolid()) {
                    walls.add(tile.getHitbox());
                }
            }
        }
        return walls;
    }

    public float getSpawnX() {
        return spawnX;
    }

    public float getSpawnY() {
        return spawnY;
    }
}
