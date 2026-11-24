package horizonSimple.electrical;

import lombok.Getter;

public class Point {
    @Getter private double potential = 0;
    @Getter private boolean fixed = false;
    private double ampere = 0;
    private double derivativeAmpere = 0;
    private final Runnable update;

    public Point(Runnable update) {
        this.update = update;
    }

    public void fixAt(float v) {
        this.fixed = true;
        this.potential = v;
        change();
    }
    public void unfixed(){
        fixed = false;
        change();
    }
    public void setPotential(double v) { if (!fixed) this.potential = v; }

    public void addAmpere(double ampere){
        this.ampere += ampere;
    }
    public void addDerivativeAmpere(double ampere){
        derivativeAmpere += ampere;
    }

    public void update(double eps){
        if (fixed) return;
        potential -= ampere/derivativeAmpere*eps;
        reset();
    }
    public void reset(){
        ampere = 0;
        derivativeAmpere = 0;
    }

    public void change() {
        if (update != null) update.run();
    }
}
