package at.freathedge.games.connected.util.loader;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class AnimationLoader {

    public static Animation loadAnimation(String basePath, int frameCount, int frameDuration) {
        Image[] frames = new Image[frameCount];
        try {
            for (int i = 0; i < frameCount; i++) {
                frames[i] = new Image(basePath + (i + 1) + ".png");
            }
        } catch (SlickException e) {
            System.err.println("Error loading animation from " + basePath + ": " + e.getMessage());
        }
        return new Animation(frames, frameDuration);
    }
}
