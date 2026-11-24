package horizonSimple.electrical.components;

import horizonSimple.electrical.Element;
import lombok.Getter;

/**
 * «Мягкий» источник ЭДС: I = g·(U − EMF). В равновесии V_input − V_output = EMF.
 * g большое — источник жёсткий, но конечное, чтобы не делить на бесконечность.
 */
@Getter
public abstract class VoltageSource extends Element {
    private static final double G = 1e5f;
    private final double emf;

    public VoltageSource(double emf) { this.emf = emf; }

    @Override public double getAmpere(double u)           { return G * (u - emf); }
    @Override public double getDerivativeAmpere(double u) { return G; }
}
