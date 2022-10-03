package drawing;

import java.awt.*;

public class Rectangle extends Shape {

    public Rectangle(String artist, long timestamp, Color color) {

        super(artist, timestamp, color);
    }

    @Override
    public void drawToGraphics(Graphics g) {
        g.fillRect(getX(), getY(), getWidth(), getHeight());
    }

}
