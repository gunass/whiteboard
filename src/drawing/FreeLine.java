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
    public int _POINT_SKIP_FACTOR = 1;

    public FreeLine(String artist, long timestamp, Color color) {
        super(artist, timestamp, color);
        points = new LinkedList<FreeLinePoint>();
    }

    public void addPoint(short x, short y) {
        points.add(new FreeLinePoint(x, y));
    }

    /**
     * A point that simply consists of two shorts
     */
    public class FreeLinePoint implements Serializable {
        short x;
        short y;
        public FreeLinePoint(short x, short y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * Draw the free line to the specified graphics context. Optionally, we can allow the paint to skip n points
     * for each point included. However, the distance-based optimisation method gives better accuracy.
     * @param g: the graphics context of the canvas
     */
    @Override
    public void drawToGraphics(Graphics g) {
        g.setColor(this.colour);
        int pointsSkipFactor = _POINT_SKIP_FACTOR;
        for (int i = pointsSkipFactor; i < points.size(); i += pointsSkipFactor) {
            // FIXME: Refine this to an arc ?
            g.drawLine(points.get(i-pointsSkipFactor).x, points.get(i-pointsSkipFactor).y, points.get(i).x, points.get(i).y);
        }
    }

    /**
     * Unique method for FreeLine which deletes all points that are not sufficiently far from the previous point,
     * saving space (and rendering time) depending on the optimisation factor used
     * A DCT or FFT would be optimal, however, I don't care.
     * @param optimisationFactor: the square of the Euclidean pixel distance desired between any two points
     */
    public void optimise(int optimisationFactor) {
        if (points.isEmpty()) return;
        LinkedList<FreeLinePoint> newPoints = new LinkedList<FreeLinePoint>();

        // Always include the first point
        newPoints.push(points.pop());

        while (!points.isEmpty()) {
            FreeLinePoint p = points.pop();
            FreeLinePoint q = newPoints.peek();

            // Include every subsequent point P[i] iff the last
            // point added is greater than the optimisation distance away
            if (Math.abs(p.x - q.x) + Math.abs(p.y - q.y) >= optimisationFactor) {
                newPoints.push(p);
            }
        }

        points = newPoints;
    }
}
