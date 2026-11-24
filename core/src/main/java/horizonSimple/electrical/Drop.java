package horizonSimple.electrical;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import horizonSimple.electrical.diagram.Diagram;

public class Drop extends Game {
    public static Skin skin;

    @Override
    public void create() {
        skin = new Skin(Gdx.files.internal("uiSkin.json"));
        setScreen(new Diagram());
    }

    @Override
    public void dispose() {
        super.dispose();
        if (skin != null) {
            skin.dispose();
            skin = null;
        }
    }
}
