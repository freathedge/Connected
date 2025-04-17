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
        map = new TiledMap("res/testmap.tmx");
        player = new Player(0, 0);
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

        // Berechne die zukünftige Hitbox des Spielers
        Rectangle futureHitbox = new Rectangle(nextX, nextY, player.getWidth(), player.getHeight());

        boolean collides = false;

        // Kollisionsprüfung mit den fixen Wandhitboxen
//        for (Rectangle wall : map.getWallHitboxes()) {
//            // Die Wandhitboxen sind fix auf den Weltkoordinaten
//            if (futureHitbox.intersects(wall)) {
//                collides = true;
//                break;
//            }
//        }

        // Wenn keine Kollision vorliegt, die Position des Spielers aktualisieren
        if (!collides) {
            player.setPosition(nextX, nextY);
        }

        // Kamera aktualisieren (beeinflusst nur das Rendering, nicht die Positionen der Objekte)
        camera.update(player.getX(), player.getY(), gc.getWidth(), gc.getHeight(), delta);
    }

    @Override
    public void render(GameContainer gc, Graphics g) throws SlickException {
        camera.apply(g);

        map.render(0, 0, 0);

        // Spieler rendern
        player.render(g);

        // Debug: Zeichne Hitboxen der Wände (Die Hitboxen sind fix und werden nicht verschoben)
        g.setColor(Color.red);
//        for (Rectangle wall : map.getWallHitboxes()) {
//            // Wandhitbox bleibt fix an den Weltkoordinaten
//            g.draw(wall); // Hier wird nur die Hitbox gezeichnet, aber nicht beeinflusst
//        }

        // Debug: Zeichne Hitbox des Spielers
        g.setColor(Color.green);
        g.draw(player.getHitbox()); // Spielerhitbox bleibt ebenfalls fix
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
