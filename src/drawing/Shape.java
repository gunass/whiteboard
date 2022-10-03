package drawing;

import java.awt.*;

/**
 * Parent class of all shapes, that is, Drawings that are created by dragging the mouse, but are not free lines
 * @author Alex Epstein
 */
public abstract class Shape extends Drawing {

    public Shape(String artist, long timestamp, Color color) {
        super(artist, timestamp, color);
    }

    // Methods for getting orientation-independent coordinates for drawing

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
