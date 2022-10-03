package drawing;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Free line stored as a series of points
 * @author Alex Epstein
 */
public class FreeLine extends Drawing {


    public ArrayList<FreeLinePoint> points;

    public FreeLine(String artist, long timestamp, Color color) {
        super(artist, timestamp, color);
        points = new ArrayList<>();
    }

    public void addPoint(int x, int y) {
        points.add(new FreeLinePoint(x, y));
    }

    public class FreeLinePoint implements Serializable {
        int x;
        int y;
        public FreeLinePoint(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    @Override
    public void drawToGraphics(Graphics g) {
        for (int i = 1; i < points.size(); i += 1) {
            g.drawLine(points.get(i-1).x, points.get(i-1).y, points.get(i).x, points.get(i).y);
        }
    }
}
