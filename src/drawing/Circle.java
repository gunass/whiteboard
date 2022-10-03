package drawing;

import java.awt.*;

public class Circle extends Shape {

    public Circle(String artist, long timestamp, Color color) {

        super(artist, timestamp, color);
    }

    @Override
    public void drawToGraphics(Graphics g) {
        g.fillOval(getX(), getY(), getWidth(), getHeight());
    }


}
