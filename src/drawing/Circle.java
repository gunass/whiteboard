package drawing;

import java.awt.*;

/**
 * A circle, or more accurately an *ellipse*
 * @author Alex Epstein
 */
public class Circle extends Shape {

    public Circle(String artist, long timestamp, Color color) {

        super(artist, timestamp, color);
    }

    @Override
    public void drawToGraphics(Graphics g) {
        g.fillOval(getX(), getY(), getWidth(), getHeight());
    }


}
