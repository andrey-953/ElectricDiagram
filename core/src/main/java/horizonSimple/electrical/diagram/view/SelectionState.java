package horizonSimple.electrical.diagram.view;

import horizonSimple.electrical.Element;
import horizonSimple.electrical.Point;
import lombok.Getter;

public class SelectionState {
    @Getter private Point point;
    @Getter private Element element;

    public void selectPoint(Point p)     { this.point = p; this.element = null; }
    public void selectElement(Element e) { this.element = e; this.point = null; }

    public void clear() { point = null; element = null; }

    public boolean hasPoint()   { return point != null; }
    public boolean hasElement() { return element != null; }
    public boolean isEmpty()    { return point == null && element == null; }
}
