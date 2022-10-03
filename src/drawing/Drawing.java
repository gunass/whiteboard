package drawing;

import java.awt.*;
import java.io.Serializable;

/**
 * FIXME: STUB
 * Parent class of all drawing types, subdivided into free line, text, shape.
 * Should inherit various properties from Java 2D API
 * @author Alex Epstein
 */
public abstract class Drawing implements Serializable {

    public int startx;
    public int starty;
    public int endx;
    public int endy;
    public Color colour;
    String artist;
    public long timestamp;

    /**
     * Create a new drawing with the provided artist's signature
     * @param artist
     */
    public Drawing(String artist, long timestamp, Color color) {
        this.artist = artist;
        this.timestamp = timestamp;
        this.colour = color;
    }

    /**
     * Draws the selected Drawing to the graphics context.
     * @param g: the graphics context of the canvas
     */
    public void drawToGraphics(Graphics g) {
    };



}
