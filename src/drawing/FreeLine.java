package drawing;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Free line stored as a series of points
 * @author Alex Epstein
 */
public class FreeLine extends Drawing {


    public LinkedList<FreeLinePoint> points;

    // Optimise the drawing process by specifying how many points are skipped before one is drawn.
    // 1 by default but can be changed based on various requirements
    public int _POINT_SKIP_FACTOR = 1;

    public FreeLine(String artist, long timestamp, Color color) {
        super(artist, timestamp, color);
        points = new LinkedList<FreeLinePoint>();
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
        int pointsSkipFactor = _POINT_SKIP_FACTOR;
        for (int i = pointsSkipFactor; i < points.size(); i += pointsSkipFactor) {
            // FIXME: Refine this to an arc for smoother free lines
            g.drawLine(points.get(i-pointsSkipFactor).x, points.get(i-pointsSkipFactor).y, points.get(i).x, points.get(i).y);
        }
    }

    /**
     * Unique method for FreeLine which deletes a certain proportion of points from the representation
     * This lowers storage costs and simplifies drawing
     * @param optimisationFactor: ratio of the old points to the new points, i.e. 5 reduces size to 20% of the original
     */
    public void optimise(int optimisationFactor) {
        LinkedList<FreeLinePoint> newPoints = new LinkedList<FreeLinePoint>();
        for (int i = 0; i < points.size(); i += optimisationFactor) {
            newPoints.add(points.get(i));
        }
        points = newPoints;
    }
}
