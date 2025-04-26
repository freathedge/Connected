package at.freathedge.games.connected;

import at.freathedge.games.connected.spawner.EnemySpawner;
import at.freathedge.games.connected.ui.UIButton;
import org.newdawn.slick.*;
import org.newdawn.slick.tiled.TiledMap;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Connected extends BasicGame {
    private Player player;
    private Camera camera;
    private TiledMap map;
    private int playerSpawnerX, playerSpawnerY;
    private final List<EnemySpawner> spawners = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();

    private UIButton quitButton;
    private UIButton resumeButton;

    private boolean paused = false;

    public Connected(String title) throws SlickException {
        super(title);
    }

    @Override
    public void init(GameContainer gc) throws SlickException {
        map = new TiledMap("Tiled/spawnmap.tmx", "Tiled");
        loadPlayerSpawner();
        player = new Player(playerSpawnerX, playerSpawnerY, map);
        //player = new Player(1000, 1000, map);
        camera = new Camera();
        camera.setMap(map);
        camera.zoom(3.5f); //default: 3.5f

        java.awt.Font awtFont = new java.awt.Font("Verdana", java.awt.Font.BOLD, 24);
        TrueTypeFont font = new TrueTypeFont(awtFont, false);
        quitButton = new UIButton((float) (gc.getWidth() - 200) / 2, (float) (gc.getHeight() - 150) / 2, 200, 50, "Spiel beenden", font);
        resumeButton = new UIButton((float) (gc.getWidth() - 200) / 2, (float) (gc.getHeight() - 250) / 2, 200, 50, "Fortsetzen", font);

        loadEnemySpawner();
    }



    @Override
    public void update(GameContainer gc, int delta) throws SlickException {
        Input input = gc.getInput();

        if (input.isKeyPressed(Input.KEY_ESCAPE)) {
            paused = !paused;
        }

        if (paused) {
            if (quitButton.isClicked(input)) {
                gc.exit();
            }

            if (resumeButton.isClicked(input)) {
                paused = false;
            }
            return;
        }

        boolean up = input.isKeyDown(Input.KEY_W);
        boolean down = input.isKeyDown(Input.KEY_S);
        boolean left = input.isKeyDown(Input.KEY_A);
        boolean right = input.isKeyDown(Input.KEY_D);
        boolean leftClick = input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON);

        if (input.isKeyDown(Input.KEY_SPACE)) {
            player.damage(5);
        }

        player.move(delta, up, down, left, right);
        if (leftClick) {
            float mouseScreenX = input.getMouseX();
            float mouseScreenY = input.getMouseY();
            float mouseWorldX = camera.getX() + mouseScreenX / camera.getZoom();
            float mouseWorldY = camera.getY() + mouseScreenY / camera.getZoom();
            player.punch(leftClick, mouseWorldX, mouseWorldY);
        }

        player.update(delta);
        camera.update(player.getX(), player.getY(), gc.getWidth(), gc.getHeight(), delta);

        for (EnemySpawner spawner : spawners) {
            Enemy newEnemy = spawner.update(delta, player, map);
            if (newEnemy != null) {
                enemies.add(newEnemy);
            }
        }

        for (Enemy enemy : enemies) {
            enemy.update(delta);
        }

    }

    @Override
    public void render(GameContainer gc, Graphics g) throws SlickException {
        camera.apply(g);

        float mouseScreenX = gc.getInput().getMouseX();
        float mouseScreenY = gc.getInput().getMouseY();
        float mouseWorldX = camera.getX() + mouseScreenX / camera.getZoom();
        float mouseWorldY = camera.getY() + mouseScreenY / camera.getZoom();

        // Sichtbarer Bereich der Kamera in Weltkoordinaten
        float camX = camera.getX();
        float camY = camera.getY();
        float camW = gc.getWidth() / camera.getZoom();
        float camH = gc.getHeight() / camera.getZoom();

        int tileWidth = map.getTileWidth();
        int tileHeight = map.getTileHeight();

        // Kachelkoordinaten für den sichtbaren Bereich
        int startX = (int) (camX / tileWidth);
        int startY = (int) (camY / tileHeight);
        int endX = (int) Math.ceil((camX + camW) / tileWidth);
        int endY = (int) Math.ceil((camY + camH) / tileHeight);

        // Begrenzung auf die Mapgröße
        startX = Math.max(0, startX);
        startY = Math.max(0, startY);
        endX = Math.min(map.getWidth(), endX);
        endY = Math.min(map.getHeight(), endY);

        for (int i = 0; i < map.getLayerCount(); i++) {
            if (map.getLayerIndex("player") == i) {
                player.render(g, mouseWorldX, mouseWorldY);
                continue;
            }

            for (int x = startX; x < endX; x++) {
                for (int y = startY; y < endY; y++) {
                    int tileID = map.getTileId(x, y, i);
                    if (tileID != 0) {
                        Image tileImage = map.getTileImage(x, y, i);
                        if (tileImage != null) {
                            tileImage.draw(x * tileWidth, y * tileHeight);
                        }
                    }
                }
            }
        }

        for (Enemy enemy : enemies) {
            enemy.render(g);
        }

        camera.renderPlayerHealtbar(g, player);

        if (paused) {
            g.setColor(new Color(0, 0, 0, 0.5f));
            g.fillRect(camera.getX(), camera.getY(), gc.getWidth(), gc.getHeight());

            g.resetTransform();

            quitButton.render(g, gc.getInput());
            resumeButton.render(g, gc.getInput());
        }
    }

    private void loadEnemySpawner() {
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







    public static void main(String[] args) {
        try {
            AppGameContainer container = new AppGameContainer(new Connected("Connected"));
            container.setDisplayMode(1920, 1080, true);
            container.setIcon("res/icon.png");
            container.setShowFPS(true);
            container.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }


}
