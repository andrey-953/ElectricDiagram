package horizonSimple.electrical.diagram.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import horizonSimple.electrical.Element;
import horizonSimple.electrical.Point;
import horizonSimple.electrical.diagram.DiagramContext;
import horizonSimple.electrical.diagram.view.SelectionState;

public class ParametersPanel extends Table {
    private final SelectionState state;
    private final Table parameters;
    private final Label name, voltage, ampere;

    public ParametersPanel(DiagramContext context) {
        super(context.skin);
        this.state = context.selection;

        add("Parameters").center().pad(2).row();

        parameters = new Table(context.skin);
        name    = new Label(null, context.skin);
        voltage = new Label(null, context.skin);
        ampere  = new Label(null, context.skin);

        parameters.add(name).expandX().center().pad(5).row();
        parameters.add(voltage).expandX().center().padBottom(6).row();
        parameters.add(ampere).expandX().center().padBottom(3).row();

        add(new ScrollPane(parameters, context.skin)).grow();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        updateInformation();
    }

    public void updateInformation(){
        if (state.isEmpty()) {
            name.setText("Nothing selected");
            voltage.setText(null);
            ampere.setText(null);
            return;
        }
        if (state.hasPoint()) {
            Point p = state.getPoint();
            name.setText("Point");
            voltage.setText(String.format("Potential: %.4f V", p.getPotential()));
            ampere.setText(p.isFixed() ? "(ground)" : null);
            return;
        }
        if (state.hasElement()) {
            Element element = state.getElement();
            double u = element.getVoltage();
            name.setText(element.getInfo().getName());
            voltage.setText(String.format("Voltage: %.4f V", u));
            ampere.setText(String.format("Ampere: %.4f A", element.getAmpere()));
        }
    }
}
