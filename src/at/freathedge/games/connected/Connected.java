package at.freathedge.games.connected;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.tiled.TiledMap;

import java.util.ArrayList;
import java.util.List;

public class Connected extends BasicGame {

    private Player player;
    private Camera camera;
    private TiledMap map;

    public Connected(String title) throws SlickException {
        super(title);
    }

    @Override
    public void init(GameContainer gc) throws SlickException {
        map = new TiledMap("res/Testmap/Testmap.tmx", "res/Testmap");
        System.out.println("map: " + map.getWidth() + ", " + map.getHeight());
        player = new Player((map.getWidth() * map.getTileWidth()) / 2, (map.getHeight() * map.getTileHeight()) / 2);
        camera = new Camera();
    }

    @Override
    public void update(GameContainer gc, int delta) throws SlickException {
        Input input = gc.getInput();
        float speed = 0.3f * delta;

        float nextX = player.getX();
        float nextY = player.getY();

        if (input.isKeyDown(Input.KEY_W)) nextY -= speed;
        if (input.isKeyDown(Input.KEY_S)) nextY += speed;
        if (input.isKeyDown(Input.KEY_A)) nextX -= speed;
        if (input.isKeyDown(Input.KEY_D)) nextX += speed;

        if (!isBlocked(nextX, player.getY())) {
            player.setX(nextX);
        }
        if (!isBlocked(player.getX(), nextY)) {
            player.setY(nextY);
        }

        camera.update(player.getX(), player.getY(), gc.getWidth(), gc.getHeight(), delta);
    }

    @Override
    public void render(GameContainer gc, Graphics g) throws SlickException {
        camera.apply(g);

        for (int i = 0; i < map.getLayerCount(); i++) {
            map.render(0, 0, i);
        }

        player.render(g);

    }

    private boolean isBlocked(float x, float y) {
        int tileSize = map.getTileWidth(); // oder getTileHeight(), sollten gleich sein
        int wallLayer = 1; // Die Wände sind in Layer 1 (achte auf den richtigen Index)

        // Spielergröße in Pixeln
        int playerWidth = 40;
        int playerHeight = 40;

        // Alle 4 Ecken des Spielers berechnen
        int[][] checkPoints = {
                { (int)(x / tileSize),             (int)(y / tileSize) }, // oben links
                { (int)((x + playerWidth - 1) / tileSize), (int)(y / tileSize) }, // oben rechts
                { (int)(x / tileSize),             (int)((y + playerHeight - 1) / tileSize) }, // unten links
                { (int)((x + playerWidth - 1) / tileSize), (int)((y + playerHeight - 1) / tileSize) } // unten rechts
        };

        for (int[] point : checkPoints) {
            int tileX = point[0];
            int tileY = point[1];

            // Sicherheit: Kollision außerhalb der Map vermeiden
            if (tileX < 0 || tileY < 0 || tileX >= map.getWidth() || tileY >= map.getHeight()) {
                return true; // Rand der Map blockiert
            }

            int tileID = map.getTileId(tileX, tileY, wallLayer);
            if (tileID != 0) return true; // Blockiert, wenn Tile vorhanden
        }

        return false; // Alle Ecken frei
    }


    public static void main(String[] args) {
        try {
            AppGameContainer container = new AppGameContainer(new Connected("Connected"));
            container.setDisplayMode(1920, 1080, false);
            container.setShowFPS(false);
            container.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }
}
