package at.freathedge.games.connected;

import at.freathedge.games.connected.util.map.BetterTiledMap;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.tiled.TiledMap;

public class GameMap {
    private BetterTiledMap map;

    public GameMap(BetterTiledMap map) throws SlickException {
        this.map = map;
    }

    public BetterTiledMap getMap() {
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
}
