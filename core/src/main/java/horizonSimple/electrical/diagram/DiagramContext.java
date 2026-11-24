package horizonSimple.electrical.diagram;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import horizonSimple.electrical.ElectricalNetwork;
import horizonSimple.electrical.diagram.view.ElementsLocation;
import horizonSimple.electrical.diagram.view.SelectionState;

public class DiagramContext {
    public ElectricalNetwork network;
    public ElementsLocation location;
    public Skin skin;
    public SelectionState selection;
    public OrthographicCamera camera;
}
