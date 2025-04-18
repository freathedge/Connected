package at.freathedge.games.connected;

import at.freathedge.games.connected.spawner.EnemySpawner;
import at.freathedge.games.connected.ui.UIButton;
import org.newdawn.slick.*;
import org.newdawn.slick.tiled.TiledMap;

import java.util.ArrayList;
import java.util.List;

public class Connected extends BasicGame {
    private Player player;
    private Camera camera;
    private TiledMap map;
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
        map = new TiledMap("res/Map/Map.tmx", "res/Map");
        player = new Player((float) (map.getWidth() * map.getTileWidth()) / 2, (float) (map.getHeight() * map.getTileHeight()) / 2, map);
        camera = new Camera();
        camera.setMap(map);
        camera.zoom(2f);

        java.awt.Font awtFont = new java.awt.Font("Verdana", java.awt.Font.BOLD, 24);
        TrueTypeFont font = new TrueTypeFont(awtFont, false);
        quitButton = new UIButton(gc, (float) (gc.getWidth() - 200) / 2, (float) (gc.getHeight() - 150) / 2, 200, 50, "Spiel beenden", font);
        resumeButton = new UIButton(gc, (float) (gc.getWidth() - 200) / 2, (float) (gc.getHeight() - 250) / 2, 200, 50, "Fortsetzen", font);

//        int spawnerLayerIndex = map.getLayerIndex("spawner.enemy");
//        for (int x = 0; x < map.getWidth(); x++) {
//            for (int y = 0; y < map.getHeight(); y++) {
//                int tileId = map.getTileId(x, y, spawnerLayerIndex);
//                if (tileId != 0) {
//                    float worldX = x * map.getTileWidth();
//                    float worldY = y * map.getTileHeight();
//                    spawners.add(new EnemySpawner(worldX, worldY));
//                }
//            }
//        }
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

        player.move(delta, up, down, left, right);
        player.punch(leftClick);
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

        for (int i = 0; i < map.getLayerCount(); i++) {
            map.render(0, 0, i);
        }

        player.render(g);

        for (Enemy enemy : enemies) {
            enemy.render(g);
        }


        if (paused) {
            g.setColor(new Color(0, 0, 0, 0.5f));
            g.fillRect(camera.getX(), camera.getY(), gc.getWidth(), gc.getHeight());

            g.resetTransform();

            quitButton.render(g, gc.getInput());
            resumeButton.render(g, gc.getInput());
        }
    }


    public static void main(String[] args) {
        try {
            AppGameContainer container = new AppGameContainer(new Connected("Connected"));
            container.setDisplayMode(1920, 1080, true);
            container.setShowFPS(false);
            container.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }
}
