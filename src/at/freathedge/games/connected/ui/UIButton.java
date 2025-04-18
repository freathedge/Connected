package at.freathedge.games.connected.ui;

import org.lwjgl.input.Cursor;
import org.lwjgl.util.Display;
import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;

public class UIButton {



    private Rectangle bounds;
    private String label;
    private Font font;

    private Color backgroundColor = new Color(60, 60, 60);
    private Color hoverColor = new Color(90, 90, 90);
    private Color textColor = Color.white;
    private Color borderColor = new Color(255, 255, 255, 100);

    public UIButton(float x, float y, float width, float height, String label, Font font) {
        this.bounds = new Rectangle(x, y, width, height);
        this.label = label;
        this.font = font;
    }

    public void render(Graphics g, Input input) throws SlickException {
        boolean hovered = bounds.contains(input.getMouseX(), input.getMouseY());

        g.setColor(hovered ? hoverColor : backgroundColor);
        g.fill(bounds);

        g.setColor(borderColor);
        g.draw(bounds);

        g.setColor(textColor);
        float textX = bounds.getX() + (bounds.getWidth() - font.getWidth(label)) / 2f;
        float textY = bounds.getY() + (bounds.getHeight() - font.getHeight(label)) / 2f;
        font.drawString(textX, textY, label);

    }

    public boolean isClicked(Input input) {
        return bounds.contains(input.getMouseX(), input.getMouseY()) && input.isMousePressed(Input.MOUSE_LEFT_BUTTON);
    }

    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
    }

    public void setHoverColor(Color color) {
        this.hoverColor = color;
    }

    public void setTextColor(Color color) {
        this.textColor = color;
    }

    public void setBorderColor(Color color) {
        this.borderColor = color;
    }
}
