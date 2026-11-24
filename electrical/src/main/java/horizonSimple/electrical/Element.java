package horizonSimple.electrical;

import horizonSimple.electrical.packet.ElementInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public abstract class Element {
    private Point input;
    private Point output;

    public abstract ElementInfo getInfo();

    public double getVoltage(){
        return input.getPotential()-output.getPotential();
    }
    public double getAmpere(){
        return getAmpere(getVoltage());
    }

    protected abstract double getAmpere(double voltage);

    public double getDerivativeAmpere(double voltage){
        return (getAmpere(voltage+0.001)-getAmpere(voltage-0.001))/0.002;
    }

    public void calculate(){
        double voltage = getVoltage();
        double ampere = getAmpere(voltage);
        double derivativeAmpere = getDerivativeAmpere(voltage);

        input.addAmpere(ampere);
        input.addDerivativeAmpere(derivativeAmpere);

        output.addAmpere(-ampere);
        output.addDerivativeAmpere(derivativeAmpere);
    }

    public List<Parameter> getParameters(){
        return List.of();
    }
}
