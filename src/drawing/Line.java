package drawing;

import java.awt.*;

public class Line extends Drawing {

    public Line(String artist, long timestamp, Color color) {
        super(artist, timestamp, color);
    }

    @Override
    public void drawToGraphics(Graphics g) {
        g.drawLine(startx, starty, endx, endy);
    }
}
