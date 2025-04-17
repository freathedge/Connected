package at.freathedge.games.connected.spawner;

import at.freathedge.games.connected.Enemy;
import at.freathedge.games.connected.Player;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.tiled.TiledMap;

import java.util.Random;

public class EnemySpawner {
    private float x, y;
    private int spawnCooldown;
    private int timeSinceLastSpawn = 0;
    private Random random = new Random();

    public EnemySpawner(float x, float y) {
        this.x = x;
        this.y = y;
        this.spawnCooldown = 30000 + random.nextInt(60000);
    }

    public Enemy update(int delta, Player player, TiledMap map) throws SlickException {
        timeSinceLastSpawn += delta;
        if (timeSinceLastSpawn >= spawnCooldown) {
            timeSinceLastSpawn = 0;
            spawnCooldown = 1000 + random.nextInt(3000);
            return new Enemy(x, y,player , map);
        }
        return null;
    }
}
