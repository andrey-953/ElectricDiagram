package horizonSimple.electrical.diagram.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import java.util.List;

import horizonSimple.electrical.packet.ElementInfo;
import horizonSimple.electrical.packet.ElementsPacket;

public class ElementsTable extends Table {
    public interface Listener {
        void onElementSelected(ElementInfo info);
    }

    private final Skin skin;
    private final Listener listener;
    private final ButtonGroup<TextButton> group = new ButtonGroup<>();

    public ElementsTable(Skin skin, List<ElementsPacket> packets, Listener listener) {
        this.skin = skin;
        this.listener = listener;
        group.setMinCheckCount(0);
        group.setMaxCheckCount(1);
        build(packets);
    }

    private void build(List<ElementsPacket> packets) {
        top();
        for (ElementsPacket packet : packets) addPacket(packet);
    }

    private void addPacket(ElementsPacket packet) {
        Table header = new Table(skin);
        header.add(new Label(packet.getName(), skin)).left();
        add(header).grow().padTop(8).padBottom(4).row();

        for (ElementInfo info : packet.getElements()) {
            TextButton btn = new TextButton(info.getName(), skin);
            btn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent e, Actor a) {
                    if (!btn.isChecked()) return;
                    listener.onElementSelected(info);
                }
            });
            group.add(btn);
            add(btn).fillX().pad(2).row();
        }
    }

    public void clearSelection() {
        for (TextButton btn : group.getButtons()) {
            if (btn.isChecked()) btn.setChecked(false);
        }
    }
}
