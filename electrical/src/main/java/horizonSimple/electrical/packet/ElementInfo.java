package horizonSimple.electrical.packet;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import horizonSimple.electrical.Element;

public interface ElementInfo {
    public String getName();
    public Element create();
    public Drawable getDrawable();
}
