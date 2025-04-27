package at.freathedge.games.connected;

import at.freathedge.games.connected.ui.UIButton;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.tiled.TiledMap;

import java.awt.*;

public class Camera {
    private float x, y;
    private float targetX, targetY;
    private float lerpSpeed = 0.05f;

    private float zoomFactor = 1.0f;
    private float screenWidth, screenHeight;



    private GameMap map;

    public Camera() {
        this.x = 0;
        this.y = 0;
        this.targetX = 0;
        this.targetY = 0;
    }

    public void setMap(GameMap map) {
        this.map = map;
    }


    public void update(float playerX, float playerY, float screenWidth, float screenHeight, int delta) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        // Bildschirmhalbwerte angepasst an aktuellen Zoom
        float halfW = screenWidth  / (2f * zoomFactor);
        float halfH = screenHeight / (2f * zoomFactor);

        // Zielposition so setzen, dass Spieler zentriert ist
        targetX = playerX - halfW;
        targetY = playerY - halfH;

        // Maximalen Kamerabereich berechnen (Mapgröße minus sichtbarer Bereich)
        float maxX = map.getWidth()  * map.getTileWidth()  - screenWidth  / zoomFactor;
        float maxY = map.getHeight() * map.getTileHeight() - screenHeight / zoomFactor;

        // Clamp, damit die Kamera nicht über die Map-Ränder hinausgeht
        targetX = Math.max(0, Math.min(targetX, maxX));
        targetY = Math.max(0, Math.min(targetY, maxY));

        // Sanftes Nachziehen (Lerp)
        x += (targetX - x) * lerpSpeed * (delta / 16.0f);
        y += (targetY - y) * lerpSpeed * (delta / 16.0f);
    }

    /** Wendet Scale und Translation an, bevor gezeichnet wird */
    public void apply(Graphics g) {
        g.scale(zoomFactor, zoomFactor);
        g.translate(-x, -y);
    }

    /** Setzt den Zoomfaktor (1.0 = 100%) */
    public void zoom(float factor) {
        this.zoomFactor = factor;
    }

    /** Zeichnet Text relativ zur Kamera */
    public void drawText(String text, float screenX, float screenY) {
        Font awtFont = new Font("Poppins", Font.PLAIN, 20);
        TrueTypeFont font = new TrueTypeFont(awtFont, false);
        font.drawString(x + screenX, y + screenY, text);
    }

    /** Rendert die Lebensanzeige des Spielers oben links im Viewport */
    public void renderPlayerHealtbar(Graphics g, Player player) {
        float healthPercentage = (float) player.health() / player.maxHealth();

        // Hintergrund
        g.setColor(Color.darkGray);
        g.fillRect(x + 10, y + 10, 200, 20);
        // Gesundheitsanzeige
        g.setColor(Color.red);
        g.fillRect(x + 10, y + 10, 200 * healthPercentage, 20);
        // Umrandung
        g.setColor(Color.black);
        g.drawRect(x + 10, y + 10, 200, 20);
    }

    // Getter für Position und Zoom
    public float getZoom() {
        return zoomFactor;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
