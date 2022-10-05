package drawing;

import java.awt.*;

/**
 * A line :)
 * @author Alex Epstein
 */
public class Line extends Drawing {

    public Line(String artist, long timestamp, Color color) {
        super(artist, timestamp, color);
    }

    @Override
    public void drawToGraphics(Graphics g) {
        g.setColor(this.colour);
        g.drawLine(startx, starty, endx, endy);
    }
}
