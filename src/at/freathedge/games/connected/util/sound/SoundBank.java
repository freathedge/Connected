package at.freathedge.games.connected.util.sound;

import org.newdawn.slick.Sound;
import org.newdawn.slick.SlickException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SoundBank {

    private final List<Sound> sounds = new ArrayList<>();
    private final Random random = new Random();

    public void addSound(String path) {
        try {
            sounds.add(new Sound(path));
        } catch (SlickException e) {
            System.err.println("Failed to load sound: " + path + " (" + e.getMessage() + ")");
        }
    }

    public void playRandom(float pitch, float volume) {
        if (!sounds.isEmpty()) {
            sounds.get(random.nextInt(sounds.size())).play(pitch, volume);
        }
    }

    public void playSpecific(int index, float pitch, float volume) {
        if (index >= 0 && index < sounds.size()) {
            sounds.get(index).play(pitch, volume);
        }
    }

    public void stopAll() {
        for (Sound sound : sounds) {
            sound.stop();
        }
    }
}
