package at.freathedge.games.connected.util.map;

import org.newdawn.slick.SlickException;

import java.io.InputStream;

public interface TiledMapInterface {
    void load(InputStream in, String tileSetsLocation) throws SlickException;
}
