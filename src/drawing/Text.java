package drawing;

import java.awt.*;

/**
 * FIXME: STUB
 */
public class Text extends Drawing {


    private char[] chars;
    // font uses default values, but should be dynamic and user-set
    private Font font = new Font("Helvetica", Font.PLAIN, 18);

    public Text(String artist, long timestamp, Color color) {
        super(artist, timestamp, color);
    }

    public void setCharArray(char[] chars) {
        this.chars = chars;
    }

    public void drawToGraphics(Graphics g) {
        g.setColor(colour);
        g.setFont(font);
        g.drawChars(chars, 0, chars.length, startx, starty);
    }

}
