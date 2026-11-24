package horizonSimple.electrical.components;

import horizonSimple.electrical.Element;

public abstract class Diode extends Element {
    private final double is, nvt;
    private static final double U_MAX = 50f;

    public Diode(double is, double nvt) { this.is = is; this.nvt = nvt; }

    private double clamp(double u) { return Math.min(u, U_MAX); }

    @Override public double getAmpere(double u) {
        return is * (Math.exp(clamp(u) / nvt) - 1.);
    }
    @Override public double getDerivativeAmpere(double u) {
        return is / nvt * Math.exp(clamp(u) / nvt);
    }
}
