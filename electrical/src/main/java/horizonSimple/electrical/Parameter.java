package horizonSimple.electrical;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Parameter {
    public final String label;
    public final Supplier<Double> getter;
    public final Consumer<Double> setter;

    public Parameter(String label, Supplier<Double> getter, Consumer<Double> setter) {
        this.label = label;
        this.getter = getter;
        this.setter = setter;
    }
}
