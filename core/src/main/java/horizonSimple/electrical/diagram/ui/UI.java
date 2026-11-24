package horizonSimple.electrical.diagram.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import java.util.List;

import horizonSimple.electrical.diagram.DiagramContext;
import horizonSimple.electrical.packet.ElementInfo;
import horizonSimple.electrical.packet.ElementsPacket;

public class UI extends Table {
    public interface Listener {
        void onSelectMode();
        void onPointMode();
        void onElementSelected(ElementInfo info);
    }

    private final Skin skin;
    private final Stage stage;
    private final Listener listener;
    private final ElementsTable elementsTable;
    private final ButtonGroup<TextButton> modeGroup = new ButtonGroup<>();
    private final TextButton selectBtn, pointBtn;

    public UI(DiagramContext context, Stage stage, List<ElementsPacket> packets, Listener listener) {
        this.skin = context.skin;
        this.stage = stage;
        this.listener = listener;

        this.elementsTable = new ElementsTable(skin, packets, info -> {
            listener.onElementSelected(info);
            for (TextButton b : modeGroup.getButtons()) b.setChecked(false);
        });

        this.modeGroup.setMinCheckCount(0);
        this.modeGroup.setMaxCheckCount(1);

        this.selectBtn = createModeButton("Select", listener::onSelectMode);
        this.pointBtn  = createModeButton("Point",  listener::onPointMode);

        build(context);
    }

    private TextButton createModeButton(String label, Runnable action) {
        TextButton btn = new TextButton(label, skin);
        btn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                if (!btn.isChecked()) return;
                elementsTable.clearSelection();
                action.run();
            }
        });
        modeGroup.add(btn);
        return btn;
    }

    private void build(DiagramContext context) {
        setFillParent(true);

        Table right = new Table(skin);
        right.setBackground("background");
        right.add(buildRightPanel(context)).colspan(2).grow().row();
        right.add(selectBtn).pad(2).growX();
        right.add(pointBtn).pad(2).growX();

        SplitPane pane = new SplitPane(null, right, false, skin);
        add(pane).grow();
    }

    private Actor buildRightPanel(DiagramContext context) {
        Table elements = new Table(skin);
        elements.setBackground("background");
        elements.add(new Label("Elements", skin)).center().pad(4).row();

        ScrollPane paletteScroll = new ScrollPane(elementsTable, skin);
        paletteScroll.setFadeScrollBars(false);
        paletteScroll.setScrollingDisabled(true, false);
        elements.add(paletteScroll).grow();

        return new SplitPane(new ParametersPanel(context), elements, true, skin);
    }
}
