package at.freathedge.games.connected.util;

import at.freathedge.games.connected.Camera;
import at.freathedge.games.connected.GameMap;
import at.freathedge.games.connected.Player;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class MapRenderer {

    private final GameMap gameMap;
    private final Camera camera;
    private final Player player;

    public MapRenderer(GameMap gameMap, Camera camera, Player player) {
        this.gameMap = gameMap;
        this.camera = camera;
        this.player = player;
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
            // Achtung: Hier brauchen wir eine kleine Anpassung für Layer-Properties.
            // Falls du später Layer-Properties brauchst, müsstest du sie in GameMap extra abfragen.
            // Vorläufig überspringen wir die skipRender-Abfrage.

            if (playerLayerIndex != -1 && playerLayerIndex == i) {
                float mouseScreenX = gc.getInput().getMouseX();
                float mouseScreenY = gc.getInput().getMouseY();
                float mouseWorldX = camera.getX() + mouseScreenX / camera.getZoom();
                float mouseWorldY = camera.getY() + mouseScreenY / camera.getZoom();

                player.render(g, mouseWorldX, mouseWorldY);
            }

            for (int x = startX; x < endX; x++) {
                for (int y = startY; y < endY; y++) {
                    int tileID = gameMap.getTileId(x, y, i);
                    if (tileID != 0) {
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
