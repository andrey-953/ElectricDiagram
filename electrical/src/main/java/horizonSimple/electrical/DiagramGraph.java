package horizonSimple.electrical;

import java.util.IdentityHashMap;
import java.util.Map;

public class DiagramGraph {
    private final ElectricalNetwork network;
    private final Map<Point, Point> parent = new IdentityHashMap<>();
    private boolean refreshing = false;

    public DiagramGraph(ElectricalNetwork network) {
        this.network = network;
    }

    public void refresh() {
        if (refreshing) return;
        refreshing = true;

        try {
            rebuild();
            ensureGrounds();
        }finally {
            refreshing = false;
        }
    }

    public Point getRoot(Point point) {
        if (!parent.containsKey(point)) return point;
        return find(point);
    }

    private Point find(Point p) {
        Point root = p;
        while (true) {
            Point par = parent.get(root);
            if (par == null || par == root) break;
            root = par;
        }
        Point cur = p;
        while (cur != root) {
            Point next = parent.get(cur);
            parent.put(cur, root);
            cur = next;
        }
        return root;
    }

    private void union(Point a, Point b) {
        Point ra = find(a), rb = find(b);
        if (ra != rb) parent.put(rb, ra);
    }

    private void rebuild() {
        parent.clear();
        for (Element e : network.getElements()) {
            Point a = e.getInput(), b = e.getOutput();
            if (a == null || b == null) continue;
            if (!parent.containsKey(a)) parent.put(a, a);
            if (!parent.containsKey(b)) parent.put(b, b);
            union(a, b);
        }
    }

    private void ensureGrounds() {
        for (Point point : network.getPoints()) {
            point.unfixed();
            getRoot(point).fixAt(0);
        }
    }
}
