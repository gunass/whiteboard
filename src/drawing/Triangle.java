package drawing;

import java.awt.*;

/**
 * A triangle
 * FIXME: forced isosceles and always oriented "upways".
 * @author Alex Epstein
 */
public class Triangle extends Shape {

    public Triangle(String artist, long timestamp, Color color) {
        super(artist, timestamp, color);
    }

    @Override
    public void drawToGraphics(Graphics g) {

        g.setColor(this.colour);
        Polygon tri = new Polygon(new int[]{getX(), getX() + (getWidth() / 2), getX() + getWidth()},
                new int[]{getY() + getHeight(), getY(), getY() + getHeight()}, 3);

        g.fillPolygon(tri);
    }

}
