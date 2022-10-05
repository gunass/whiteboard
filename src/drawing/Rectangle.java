package drawing;

import java.awt.*;

/**
 * A rectangle
 * @author Alex Epstein
 */
public class Rectangle extends Shape {

    public Rectangle(String artist, long timestamp, Color color) {

        super(artist, timestamp, color);
    }

    @Override
    public void drawToGraphics(Graphics g) {
        g.setColor(this.colour); g.fillRect(getX(), getY(), getWidth(), getHeight());
    }

}
