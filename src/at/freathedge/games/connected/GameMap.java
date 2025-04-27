package at.freathedge.games.connected;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.tiled.TiledMap;

public class GameMap {
    private TiledMap map;

    public GameMap(TiledMap map) throws SlickException {
        this.map = map;
    }

    public TiledMap getMap() {
        return map;
    }

    public int getTileWidth() {
        return map.getTileWidth();
    }

    public int getTileHeight() {
        return map.getTileHeight();
    }

    public int getWidth() {
        return map.getWidth();
    }

    public int getHeight() {
        return map.getHeight();
    }

    public int getTileId(int x, int y, int layer) {
        return map.getTileId(x, y, layer);
    }

    public int getLayerIndex(String layerName) {
        return map.getLayerIndex(layerName);
    }

    public int getLayerCount() {
        return map.getLayerCount();
    }

    public org.newdawn.slick.Image getTileImage(int x, int y, int layer) {
        return map.getTileImage(x, y, layer);
    }

    public void renderLayer(int layer, int startX, int startY, int endX, int endY) {
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                int tileID = map.getTileId(x, y, layer);
                if (tileID != 0) {
                    org.newdawn.slick.Image tileImage = map.getTileImage(x, y, layer);
                    if (tileImage != null) {
                        tileImage.draw(x * getTileWidth(), y * getTileHeight());
                    }
                }
            }
        }
    }
}
