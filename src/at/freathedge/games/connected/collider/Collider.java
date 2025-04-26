package at.freathedge.games.connected.collider;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.tiled.TiledMap;
import org.newdawn.slick.geom.Rectangle;

import java.util.HashMap;
import java.util.Map;

public class Collider {

    private final TiledMap map;
    private final Map<Integer, Rectangle> wallTileBounds = new HashMap<>();

    public Collider(TiledMap map) throws SlickException {
        this.map = map;
        calculateTileBounds();
    }

    private void calculateTileBounds() throws SlickException {
        int wallLayer = map.getLayerIndex("wall");
        if (wallLayer == -1) return;

        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                int tileId = map.getTileId(x, y, wallLayer);
                if (tileId == 0 || wallTileBounds.containsKey(tileId)) continue;

                Image tileImage = map.getTileImage(x, y, wallLayer);
                if (tileImage == null) continue;

                int minX = tileImage.getWidth(), minY = tileImage.getHeight();
                int maxX = 0, maxY = 0;
                boolean found = false;

                for (int px = 0; px < tileImage.getWidth(); px++) {
                    for (int py = 0; py < tileImage.getHeight(); py++) {
                        if (tileImage.getColor(px, py).getAlpha() > 0.1f) {
                            found = true;
                            if (px < minX) minX = px;
                            if (py < minY) minY = py;
                            if (px > maxX) maxX = px;
                            if (py > maxY) maxY = py;
                        }
                    }
                }

                if (found) {
                    wallTileBounds.put(tileId, new Rectangle(minX, minY, (maxX - minX + 1), (maxY - minY + 1)));
                }
            }
        }
    }

    public boolean isBlocked(float worldX, float worldY) {
        int tileSize = map.getTileWidth();
        int wallLayer = map.getLayerIndex("wall");
        if (wallLayer == -1) return false;

        int tileX = (int)(worldX / tileSize);
        int tileY = (int)(worldY / tileSize);

        int tileId = map.getTileId(tileX, tileY, wallLayer);
        if (tileId == 0) return false;

        Rectangle bounds = wallTileBounds.get(tileId);
        if (bounds == null) return false;

        float localX = worldX % tileSize;
        float localY = worldY % tileSize;

        return bounds.contains(localX, localY);
    }

    public boolean isBlockedRectangle(float x, float y, float width, float height) {
        int samples = 5; // Mehr = genauer, weniger = schneller
        for (int i = 0; i <= samples; i++) {
            float sampleX = x + (width * i) / samples;
            for (int j = 0; j <= samples; j++) {
                float sampleY = y + (height * j) / samples;
                if (isBlocked(sampleX, sampleY)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void renderDebug(Graphics g) {
        int tileSize = map.getTileWidth();
        int wallLayer = map.getLayerIndex("wall");
        if (wallLayer == -1) return;

        g.setColor(new Color(255, 0, 0, 80));

        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                int tileId = map.getTileId(x, y, wallLayer);
                if (tileId == 0) continue;

                Rectangle bounds = wallTileBounds.get(tileId);
                if (bounds != null) {
                    g.fillRect(
                            x * tileSize + bounds.getX(),
                            y * tileSize + bounds.getY(),
                            bounds.getWidth(),
                            bounds.getHeight()
                    );
                }
            }
        }
    }
}