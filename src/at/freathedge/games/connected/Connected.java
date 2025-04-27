package at.freathedge.games.connected;

import at.freathedge.games.connected.spawner.EnemySpawner;
import at.freathedge.games.connected.ui.UIButton;
import at.freathedge.games.connected.util.MapHandler;
import at.freathedge.games.connected.util.MapRenderer;
import org.newdawn.slick.*;
import org.newdawn.slick.tiled.TiledMap;

import javax.swing.*;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.awt.Font;

public class Connected extends BasicGame {
    private Player player;
    private Camera camera;
    private GameMap map;
    private final List<EnemySpawner> spawners = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();

    private UIButton quitButton;
    private UIButton resumeButton;

    private boolean paused = false;

    private boolean loading = true;
    private int loadingTimer = 0;
    private final int loadingDuration = 5000; // 1000ms = 1 Sekunde Ladebildschirm
    private int loadingDotTimer = 0;
    private int loadingDotCount = 0;

    private MapHandler mapHandler;
    private MapRenderer mapRenderer;



    public Connected(String title) throws SlickException {
        super(title);
    }

    @Override
    public void init(GameContainer gc) throws SlickException {
        mapHandler = new MapHandler("Tiled/spawnmap.tmx");
        map = mapHandler.getMap();

        player = new Player(mapHandler.getPlayerSpawnerX(), mapHandler.getPlayerSpawnerY(), map.getMap());
        camera = new Camera();
        camera.setMap(map);
        camera.zoom(3.5f); //default: 3.5f

        mapRenderer = new MapRenderer(map, camera, player);

        java.awt.Font awtFont = new java.awt.Font("Verdana", java.awt.Font.BOLD, 24);
        TrueTypeFont font = new TrueTypeFont(awtFont, false);
        quitButton = new UIButton((float) (gc.getWidth() - 200) / 2, (float) (gc.getHeight() - 150) / 2, 200, 50, "Spiel beenden", font);
        resumeButton = new UIButton((float) (gc.getWidth() - 200) / 2, (float) (gc.getHeight() - 250) / 2, 200, 50, "Fortsetzen", font);

        spawners.addAll(mapHandler.getSpawners());
    }



    @Override
    public void update(GameContainer gc, int delta) throws SlickException {
        if (loading) {
            camera.update(player.getX(), player.getY(), gc.getWidth(), gc.getHeight(), delta);

            loadingTimer += delta;
            loadingDotTimer += delta;

            if (loadingDotTimer >= 300) {
                loadingDotCount = (loadingDotCount + 1) % 4;
                loadingDotTimer = 0;
            }

            if (loadingTimer >= loadingDuration) {
                loading = false;
            }
            return;
        }


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
            Enemy newEnemy = spawner.update(delta, player, map.getMap());
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
        if (loading) {
            g.setColor(Color.black);
            g.fillRect(0, 0, gc.getWidth(), gc.getHeight());

            g.setColor(Color.white);

            StringBuilder loadingText = new StringBuilder("Loading");
            for (int i = 0; i < loadingDotCount; i++) {
                loadingText.append(".");
            }

            java.awt.Font awtFont = null;
            try {
                awtFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, new java.io.FileInputStream("res/fonts/PressStart2P-Regular.ttf")).deriveFont(24f);
            } catch (Exception e) {
                e.printStackTrace();
            }

            TrueTypeFont font = new TrueTypeFont(awtFont, false);
            g.setFont(font);

            g.drawString(loadingText.toString(), gc.getWidth() / 2f - 150, gc.getHeight() / 2f - 24);
            return;
        }

        // Kamera Transformation anwenden
        camera.apply(g);

        // Map rendern
        mapRenderer.renderMap(g, gc);



        // Enemies rendern
        for (Enemy enemy : enemies) {
            enemy.render(g);
        }

        // Spieler-Lebensanzeige rendern
        camera.renderPlayerHealtbar(g, player);

        // Falls Pause-Menü aktiv
        if (paused) {
            g.setColor(new Color(0, 0, 0, 0.5f));
            g.fillRect(camera.getX(), camera.getY(), gc.getWidth(), gc.getHeight());

            // UI Buttons rendern ohne Kamera-Transformation
            g.resetTransform();

            quitButton.render(g, gc.getInput());
            resumeButton.render(g, gc.getInput());
        }
    }






    public static void main(String[] args) {
        try {
            AppGameContainer container = new AppGameContainer(new Connected("Connected"));
            container.setDisplayMode(1920, 1080, true);
            container.setIcons(new String[] {
                    "res/icon32.png",
                    "res/icon64.png",
                    "res/icon128.png"

            });
            container.setShowFPS(false);
            container.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }


}
