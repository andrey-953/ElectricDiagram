package horizonSimple.electrical.components;

import horizonSimple.electrical.Element;
import horizonSimple.electrical.Parameter;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public abstract class Resistor extends Element {
    private double resistance;

    public Resistor(double resistance) {
        if (resistance <= 0) throw new IllegalArgumentException("R > 0");
        this.resistance = resistance;
    }

    @Override public double getAmpere(double u)           { return u / resistance; }
    @Override public double getDerivativeAmpere(double u) { return 1f / resistance; }

    @Override
    public List<Parameter> getParameters() {
        return List.of(
            new Parameter("Resistance", this::getResistance, this::setResistance)
        );
    }
}
