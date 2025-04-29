package at.freathedge.games.connected.util.handler;

import at.freathedge.games.connected.GameMap;
import at.freathedge.games.connected.spawner.EnemySpawner;
import at.freathedge.games.connected.util.map.BetterTiledMap;
import org.newdawn.slick.*;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapHandler {

    private final GameMap map;
    private int playerSpawnerX, playerSpawnerY;
    private final List<EnemySpawner> spawners = new ArrayList<>();
    private final Map<Integer, Animation> animatedTiles = new HashMap<>();

    public MapHandler(String mapPath) throws SlickException {
        this.map = new GameMap(new BetterTiledMap(mapPath, "Tiled"));
        loadPlayerSpawner();
        loadEnemySpawners();
        loadAnimatedTiles();
    }

    private void loadPlayerSpawner() {
        int spawnerLayerIndex = map.getLayerIndex("spawner.player");
        if (spawnerLayerIndex != -1) {
            for (int x = 0; x < map.getWidth(); x++) {
                for (int y = 0; y < map.getHeight(); y++) {
                    if (map.getTileId(x, y, spawnerLayerIndex) != 0) {
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
                    if (map.getTileId(x, y, spawnerLayerIndex) != 0) {
                        spawners.add(new EnemySpawner(x * map.getTileWidth(), y * map.getTileHeight()));
                    }
                }
            }
        }
    }

    private void loadAnimatedTiles() throws SlickException {
        try {
            File tilesetFile = new File("Tiled/pigs.tsx");
            SpriteSheet tilesetSheet = new SpriteSheet("Tiled/Cute_Fantasy/Animals/Pig/combined_spritesheet (1).png", 16, 16);

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(tilesetFile);
            doc.getDocumentElement().normalize();
            NodeList tileList = doc.getElementsByTagName("tile");

            for (int i = 0; i < tileList.getLength(); i++) {
                Element tileElement = (Element) tileList.item(i);
                if (tileElement.getElementsByTagName("animation").getLength() > 0) {
                    int tileId = Integer.parseInt(tileElement.getAttribute("id"));
                    Animation animation = new Animation();

                    NodeList frameList = tileElement.getElementsByTagName("frame");
                    for (int j = 0; j < frameList.getLength(); j++) {
                        Element frameElement = (Element) frameList.item(j);
                        int frameTileId = Integer.parseInt(frameElement.getAttribute("tileid"));
                        int duration = Integer.parseInt(frameElement.getAttribute("duration"));

                        int tilesetWidth = tilesetSheet.getHorizontalCount();
                        Image frameImage = tilesetSheet.getSprite(frameTileId % tilesetWidth, frameTileId / tilesetWidth);

                        animation.addFrame(frameImage, duration);
                    }

                    animation.setAutoUpdate(true);
                    animatedTiles.put(tileId, animation);
                }
            }
        } catch (Exception e) {
            throw new SlickException("Fehler beim Laden der Tileset-Animationen", e);
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

    public Map<Integer, Animation> getAnimatedTiles() {
        return animatedTiles;
    }
}
