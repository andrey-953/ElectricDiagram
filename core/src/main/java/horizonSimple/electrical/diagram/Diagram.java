package horizonSimple.electrical.diagram;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import horizonSimple.electrical.Drop;
import horizonSimple.electrical.ElectricalNetwork;
import horizonSimple.electrical.Element;
import horizonSimple.electrical.Point;
import horizonSimple.electrical.components.*;
import horizonSimple.electrical.diagram.view.CircuitRenderer;
import horizonSimple.electrical.diagram.view.ElementsLocation;
import horizonSimple.electrical.diagram.view.PlacementMode;
import horizonSimple.electrical.diagram.view.SelectionState;
import horizonSimple.electrical.packet.ElementInfo;
import horizonSimple.electrical.packet.ElementsPacket;
import horizonSimple.electrical.diagram.ui.UI;

public class Diagram implements Screen {

    private final ElectricalNetwork network = new ElectricalNetwork();
    private final SelectionState selection = new SelectionState();
    private final List<ElementsPacket> packets = defaultPackets();

    private PlacementMode mode = PlacementMode.SELECT;
    private ElementInfo activeElementInfo;

    private DiagramContext context;
    private ElementsLocation location;
    private OrthographicCamera camera;
    private Stage stage;
    private UI ui;
    private CircuitRenderer renderer;
    private final Vector3 tmp = new Vector3();

    // =============================================================
    // Input
    // =============================================================
    private final InputAdapter worldInput = new InputAdapter() {
        @Override public boolean touchDown(int sx, int sy, int pointer, int button) {
            Vector2 w = world(sx, sy);
            if (button == Input.Buttons.LEFT)  return onLeftDown(w);
            if (button == Input.Buttons.RIGHT) return onRightDown(w);
            return false;
        }
        @Override public boolean touchDragged(int sx, int sy, int pointer) {
            if (!location.isDragging()) return false;
            Vector2 w = world(sx, sy);
            location.updateDrag(w.x, w.y);
            return true;
        }
        @Override public boolean touchUp(int sx, int sy, int pointer, int button) {
            if (button != Input.Buttons.LEFT || !location.isDragging()) return false;
            Vector2 w = world(sx, sy);
            Point end = location.findPoint(w.x, w.y, network.getPoints());
            if (end == null) end = createPointAt(w.x, w.y);
            Point from = location.endDrag();
            if (from != null && end != from) createElement(from, end);
            return true;
        }
        @Override public boolean keyDown(int keycode) {
            switch (keycode) {
                case Input.Keys.DEL:
                case Input.Keys.FORWARD_DEL: deleteSelected();  return true;
                case Input.Keys.ESCAPE:      selection.clear(); return true;
            }
            return false;
        }
    };

    private boolean onLeftDown(Vector2 w) {
        if (mode == PlacementMode.SELECT) {
            Point p = location.findPoint(w.x, w.y, network.getPoints());
            if (p != null) { selection.selectPoint(p); return true; }

            Element el = location.findElement(w.x, w.y, network.getElements());
            if (el != null) selection.selectElement(el);
            else            selection.clear();
            return true;
        }

        if (mode == PlacementMode.PLACE_POINT) {
            Point hit = location.findPoint(w.x, w.y, network.getPoints());
            if (hit == null) createPointAt(w.x, w.y);
            return true;
        }

        if (mode == PlacementMode.PLACE_ELEMENT) {
            if (activeElementInfo == null) return false;

            Point hit = location.findPoint(w.x, w.y, network.getPoints());
            if (hit == null) hit = createPointAt(w.x, w.y);
            location.beginDrag(hit, w.x, w.y);
            return true;
        }

        return false;
    }

    private boolean onRightDown(Vector2 w) {
        Point hit = location.findPoint(w.x, w.y, network.getPoints());
        if (hit != null) {
            if (hit.isFixed()) hit.unfixed();
            else               hit.fixAt(0f);
            return true;
        }
        return false;
    }

    // =============================================================
    // Screen
    // =============================================================
    @Override public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, 0);
        camera.update();

        location = new ElementsLocation();

        context = new DiagramContext();
        context.skin      = Drop.skin;
        context.selection = selection;
        context.network   = network;
        context.location  = location;
        context.camera    = camera;

        renderer = new CircuitRenderer(context);

        stage = new Stage(new ExtendViewport(750, 500));
        ui = new UI(context, stage, packets, new UI.Listener() {
            @Override public void onSelectMode()  { mode = PlacementMode.SELECT;      activeElementInfo = null; }
            @Override public void onPointMode()   { mode = PlacementMode.PLACE_POINT; activeElementInfo = null; }
            @Override public void onElementSelected(ElementInfo info) {
                mode = PlacementMode.PLACE_ELEMENT;
                activeElementInfo = info;
            }
        });
        stage.addActor(ui);

        Gdx.input.setInputProcessor(new InputMultiplexer(stage, worldInput));
    }

    @Override public void render(float delta) {
        stepSolve();
        renderer.draw();
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int w, int h) {
        camera.viewportWidth  = w;
        camera.viewportHeight = h;
        camera.position.set(w / 2f, h / 2f, 0);
        camera.update();
        stage.getViewport().update(w, h, true);
    }

    @Override public void pause()  { }
    @Override public void resume() { }
    @Override public void hide()   { Gdx.input.setInputProcessor(null); }

    @Override public void dispose() {
        renderer.dispose();
        stage.dispose();
    }

    // =============================================================
    // Модель: создание
    // =============================================================
    private Point createPointAt(float x, float y) {
        Point p = network.createPoint();
        location.placePoint(p, x, y);
        return p;
    }

    private void addElement(Element e, Point a, Point b) {
        e.setInput(a);
        e.setOutput(b);
        network.addElement(e);
    }

    private void createElement(Point a, Point b) {
        if (activeElementInfo == null) return;
        addElement(activeElementInfo.create(), a, b);
    }

    private void deleteSelected() {
        if (selection.hasPoint()) {
            Point p = selection.getPoint();
            network.removePoint(p);
            location.removePoint(p);
            selection.clear();
        } else if (selection.hasElement()) {
            network.removeElement(selection.getElement());
            selection.clear();
        }
    }

    private void clearAll() {
        network.clear();
        location.clear();
        selection.clear();
    }

    // =============================================================
    // Solve
    // =============================================================
    private void stepSolve() {
        // фиксированное число итераций на кадр — стабильнее, чем busy-loop
        for (int i = 0; i < 10; i++) network.update();
    }

    // =============================================================
    // Утилиты
    // =============================================================
    private Vector2 world(int sx, int sy) {
        tmp.set(sx, sy, 0);
        camera.unproject(tmp);
        return new Vector2(tmp.x, tmp.y);
    }

    // =============================================================
    // Палитра
    // =============================================================
    private static List<ElementsPacket> defaultPackets() {
        return List.of(
            packet("Basics", List.of(
                info("Resistor",       (info) -> new Resistor(1000f){
                    @Override
                    public ElementInfo getInfo() {
                        return info;
                    }
                }, null),
                info("Voltage Source", (info) -> new VoltageSource(5f){
                    @Override
                    public ElementInfo getInfo() {
                        return info;
                    }
                }, null),
                info("Current Source", (info) -> new CurrentSource(1e-3f){
                    @Override
                    public ElementInfo getInfo() {
                        return info;
                    }
                }, null),
                info("Diode", (info) -> new Diode(1e-6f, 0.1f){
                    @Override
                    public ElementInfo getInfo() {
                        return info;
                    }
                }, "diode.png")
            ))
        );
    }

    private static ElementInfo info(String name, Function<ElementInfo, Element> factory, String file) {
        return new ElementInfo() {
            @Override public String getName() { return name; }
            @Override public Element create() { return factory.apply(this); }
            @Override public Drawable getDrawable() {if (file == null) return null; return new TextureRegionDrawable(new Texture(Gdx.files.internal(file)));}
        };
    }

    private static ElementsPacket packet(String name, List<ElementInfo> els) {
        return new ElementsPacket() {
            @Override public String getName()     { return name; }
            @Override public List<ElementInfo> getElements() { return els; }
        };
    }
}
