package horizonSimple.electrical;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class ElectricalNetwork {
    @Getter @Setter private float eps = 0.4f;
    @Getter private final List<Point> points = new ArrayList<>();
    @Getter private final List<Element> elements = new ArrayList<>();
    private final DiagramGraph graph = new DiagramGraph(this);

    public Point createPoint() {
        Point point = new Point(graph::refresh);
        points.add(point);
        return point;
    }
    public void addElement(Element e) {
        elements.add(e);
        e.getInput().change();
        e.getOutput().change();
    }

    public List<Element> removePoint(Point p) {
        List<Element> toRemove = new ArrayList<>();
        List<Point> affected  = new ArrayList<>();
        for (Element e : elements) {
            if (e.getInput() == p || e.getOutput() == p) {
                toRemove.add(e);
                Point other = e.getInput() == p ? e.getOutput() : e.getInput();
                if (other != null && other != p) affected.add(other);
            }
        }
        elements.removeAll(toRemove);
        points.remove(p);
        for (Point pt : affected) pt.change();
        return toRemove;
    }

    public void removeElement(Element e) {
        elements.remove(e);
        if (e.getInput()  != null) e.getInput().change();
        if (e.getOutput() != null) e.getOutput().change();
    }

    public void clear() {
        points.clear();
        elements.clear();
    }

    public void update() {
        for (Point p : points) p.reset();
        for (Element e : elements) e.calculate();
        for (Point p : points) p.update(eps);
    }
}
