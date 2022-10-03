package drawing;

import java.awt.*;

/**
 * FIXME: STUB
 */
public abstract class Shape extends Drawing {

    public Shape(String artist, long timestamp, Color color) {
        super(artist, timestamp, color);
    }

    public int getX() {
        if (startx < endx) {
            return startx;
        } else {
            return endx;
        }
    }

    public int getY() {
        if (starty < endy) {
             return starty;
        } else {
            return endy;
        }
    }

    public int getWidth() {
        if (startx < endx) {
            return endx - startx;
        } else {
            return startx - endx;
        }
    }

    public int getHeight() {
        if (starty < endy) {
            return endy - starty;
        } else {
            return starty - endy;
        }
    }

}
