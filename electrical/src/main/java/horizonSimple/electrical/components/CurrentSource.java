package horizonSimple.electrical.components;

import horizonSimple.electrical.Element;

/** I = const, течёт от input к output. */
public abstract class CurrentSource extends Element {
    private final double current;
    public CurrentSource(double current) { this.current = current; }

    @Override public double getAmpere(double u)           { return current; }
    @Override public double getDerivativeAmpere(double u) { return 0f; }
    public double getCurrentValue()                      { return current; }
}
