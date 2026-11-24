package horizonSimple.electrical.diagram.view;

import com.badlogic.gdx.math.Vector2;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import horizonSimple.electrical.Element;
import horizonSimple.electrical.Point;

/**
 * Хранит позиции точек на холсте и состояние перетаскивания.
 * Hit-тесты — здесь же, чтобы Diagram и Renderer работали с одним источником истины.
 */
public class ElementsLocation {
    public static final float POINT_HIT_R   = 22f;
    public static final float ELEMENT_HIT_R = 15f;

    private final Map<Point, Vector2> positions = new IdentityHashMap<>();

    private Point dragFrom;
    private float dragX, dragY;

    public Vector2 positionOf(Point p)     { return positions.get(p); }
    public Map<Point, Vector2> positions() { return positions; }

    public void placePoint(Point p, float x, float y) {
        positions.put(p, new Vector2(x, y));
    }

    public void removePoint(Point p) { positions.remove(p); }

    public void clear() {
        positions.clear();
        dragFrom = null;
    }

    // --- hit testing ---
    public Point findPoint(float x, float y, Collection<Point> points) {
        Point best = null;
        float bestD = POINT_HIT_R * POINT_HIT_R;
        for (Point p : points) {
            Vector2 v = positions.get(p);
            if (v == null) continue;
            float d = (v.x - x) * (v.x - x) + (v.y - y) * (v.y - y);
            if (d < bestD) { bestD = d; best = p; }
        }
        return best;
    }

    public Element findElement(float x, float y, List<Element> elements) {
        Element best = null;
        float bestD = ELEMENT_HIT_R;
        for (Element e : elements) {
            Vector2 a = positions.get(e.getInput());
            Vector2 b = positions.get(e.getOutput());
            if (a == null || b == null) continue;
            float d = distanceToSegment(x, y, a.x, a.y, b.x, b.y);
            if (d < bestD) { bestD = d; best = e; }
        }
        return best;
    }

    public void beginDrag(Point from, float x, float y) {
        this.dragFrom = from;
        this.dragX = x;
        this.dragY = y;
    }

    public void updateDrag(float x, float y) {
        this.dragX = x;
        this.dragY = y;
    }

    /** Возвращает точку, от которой тянули, и сбрасывает состояние. */
    public Point endDrag() {
        Point from = dragFrom;
        dragFrom = null;
        return from;
    }

    public boolean isDragging(){ return dragFrom != null; }
    public Point   getDragFrom(){ return dragFrom; }
    public float   getDragX()  { return dragX; }
    public float   getDragY()  { return dragY; }

    // --- geometry ---
    private static float distanceToSegment(float px, float py,
                                           float ax, float ay, float bx, float by) {
        float dx = bx - ax, dy = by - ay;
        float len2 = dx * dx + dy * dy;
        if (len2 < 1e-6f) return (float) Math.hypot(px - ax, py - ay);
        float t = ((px - ax) * dx + (py - ay) * dy) / len2;
        t = Math.max(0f, Math.min(1f, t));
        return (float) Math.hypot(px - (ax + t * dx), py - (ay + t * dy));
    }
}
