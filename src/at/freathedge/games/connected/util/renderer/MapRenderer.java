package at.freathedge.games.connected.util.renderer;

import at.freathedge.games.connected.Camera;
import at.freathedge.games.connected.GameMap;
import at.freathedge.games.connected.Player;
import at.freathedge.games.connected.util.map.BetterTileSet;
import at.freathedge.games.connected.util.handler.MapHandler;
import org.newdawn.slick.*;

public class MapRenderer {

    private final MapHandler mapHandler;
    private final GameMap gameMap;
    private final Camera camera;
    private final Player player;

    public MapRenderer(GameMap gameMap, Camera camera, Player player, MapHandler mapHandler) {
        this.gameMap = gameMap;
        this.camera = camera;
        this.player = player;
        this.mapHandler = mapHandler;
    }

    public void renderMap(Graphics g, GameContainer gc) throws SlickException {
        int screenWidth = gc.getWidth();
        int screenHeight = gc.getHeight();

        float camX = camera.getX();
        float camY = camera.getY();
        float camW = screenWidth / camera.getZoom();
        float camH = screenHeight / camera.getZoom();

        int tileWidth = gameMap.getTileWidth();
        int tileHeight = gameMap.getTileHeight();

        int startX = (int) (camX / tileWidth);
        int startY = (int) (camY / tileHeight);
        int endX = (int) Math.ceil((camX + camW) / tileWidth);
        int endY = (int) Math.ceil((camY + camH) / tileHeight);

        startX = Math.max(0, startX);
        startY = Math.max(0, startY);
        endX = Math.min(gameMap.getWidth(), endX);
        endY = Math.min(gameMap.getHeight(), endY);

        int playerLayerIndex = gameMap.getLayerIndex("player");

        for (int i = 0; i < gameMap.getLayerCount(); i++) {
            if (playerLayerIndex != -1 && playerLayerIndex == i) {
                float mouseWorldX = camera.getX() + gc.getInput().getMouseX() / camera.getZoom();
                float mouseWorldY = camera.getY() + gc.getInput().getMouseY() / camera.getZoom();
                player.render(g, mouseWorldX, mouseWorldY);
            }

            for (int x = startX; x < endX; x++) {
                for (int y = startY; y < endY; y++) {
                    int tileID = gameMap.getTileId(x, y, i);
                    if (tileID != 0) {
                        BetterTileSet tileSet = (BetterTileSet) gameMap.getMap().findTileSet(tileID);

                        if (tileSet != null && tileSet.isAnimated(tileID)) {
                            Animation anim = tileSet.getAnimation(tileID);
                            anim.draw(x * tileWidth, y * tileHeight);
                        } else {
                            Image tileImage = gameMap.getTileImage(x, y, i);
                            if (tileImage != null) {
                                tileImage.draw(x * tileWidth, y * tileHeight);
                            }
                        }
                    }
                }
            }
        }
    }
}
