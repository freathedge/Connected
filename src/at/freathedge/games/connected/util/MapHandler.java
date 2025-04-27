package at.freathedge.games.connected.util;

import at.freathedge.games.connected.GameMap;
import at.freathedge.games.connected.spawner.EnemySpawner;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.tiled.TiledMap;

import java.util.ArrayList;
import java.util.List;

public class MapHandler {

    private GameMap map;
    private int playerSpawnerX, playerSpawnerY;
    private final List<EnemySpawner> spawners = new ArrayList<>();

    public MapHandler(String mapPath) throws SlickException {
        this.map = new GameMap(new TiledMap(mapPath, "Tiled"));
        loadPlayerSpawner();
        loadEnemySpawners();
    }

    private void loadPlayerSpawner() {
        int spawnerLayerIndex = map.getLayerIndex("spawner.player");
        if (spawnerLayerIndex != -1) {
            for (int x = 0; x < map.getWidth(); x++) {
                for (int y = 0; y < map.getHeight(); y++) {
                    int tileId = map.getTileId(x, y, spawnerLayerIndex);
                    if (tileId != 0) {
                        playerSpawnerX = x * map.getTileWidth();
                        playerSpawnerY = y * map.getTileHeight();
                    }
                }
            }
        }
    }

    private void loadEnemySpawners() {
        int spawnerLayerIndex = map.getLayerIndex("spawner.enemy");
        if (spawnerLayerIndex != -1) {
            for (int x = 0; x < map.getWidth(); x++) {
                for (int y = 0; y < map.getHeight(); y++) {
                    int tileId = map.getTileId(x, y, spawnerLayerIndex);
                    if (tileId != 0) {
                        float worldX = x * map.getTileWidth();
                        float worldY = y * map.getTileHeight();
                        spawners.add(new EnemySpawner(worldX, worldY));
                    }
                }
            }
        }
    }

    public GameMap getMap() {
        return map;
    }

    public int getPlayerSpawnerX() {
        return playerSpawnerX;
    }

    public int getPlayerSpawnerY() {
        return playerSpawnerY;
    }

    public List<EnemySpawner> getSpawners() {
        return spawners;
    }
}
