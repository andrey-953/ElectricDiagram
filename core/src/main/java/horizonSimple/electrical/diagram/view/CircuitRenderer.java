package horizonSimple.electrical.diagram.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import horizonSimple.electrical.ElectricalNetwork;
import horizonSimple.electrical.Element;
import horizonSimple.electrical.Point;
import horizonSimple.electrical.components.*;
import horizonSimple.electrical.diagram.DiagramContext;

public class CircuitRenderer {
    private static final float POINT_R = 10f;
    public static final Color BG = new Color(0.10f, 0.11f, 0.13f, 1f);

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();

    private final OrthographicCamera camera;
    private final ElectricalNetwork network;
    private final ElementsLocation location;
    private final SelectionState selection;

    public CircuitRenderer(DiagramContext context) {
        this.camera = context.camera;
        this.network = context.network;
        this.location = context.location;
        this.selection = context.selection;
        font.getData().setScale(1.0f);
    }

    public void draw() {
        Gdx.gl.glClearColor(BG.r, BG.g, BG.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        for (Element e : network.getElements()) drawElement(e);
        for (Point p : network.getPoints())     drawPoint(p);
        if (location.isDragging())              drawDrag();

        shapes.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawLabels();
        batch.end();
    }

    // ------------------------------------------------------------------
    // точки
    // ------------------------------------------------------------------
    private void drawPoint(Point p) {
        Vector2 v = location.positionOf(p);
        if (v == null) return;
        double t = Math.max(-1f, Math.min(1f, p.getPotential() / 10f));

        if (p == selection.getPoint()) {
            shapes.setColor(1f, 1f, 0f, 1f);
            shapes.circle(v.x, v.y, POINT_R * 1.4f, 24);
        }

        shapes.setColor((float) (0.5 + 0.5 * t), 0.25f, (float) (0.5 - 0.5 * t), 1f);
        shapes.circle(v.x, v.y, POINT_R, 20);

        if (p.isFixed()) {
            shapes.setColor(0.3f, 1f, 0.3f, 1f);
            shapes.circle(v.x, v.y, POINT_R * 0.5f, 16);
        }
    }

    // ------------------------------------------------------------------
    // элементы
    // ------------------------------------------------------------------
    private void drawElement(Element e) {
        Vector2 a = location.positionOf(e.getInput());
        Vector2 b = location.positionOf(e.getOutput());
        if (a == null || b == null) return;

        if (e == selection.getElement()) {
            shapes.setColor(1f, 1f, 0f, 0.7f);
            shapes.rectLine(a.x, a.y, b.x, b.y, 6f);
        }
        shapes.setColor(0.65f, 0.65f, 0.7f, 1f);
        shapes.rectLine(a.x, a.y, b.x, b.y, 2f);

        if      (e instanceof Resistor)      drawResistorSymbol(a, b);
        else if (e instanceof VoltageSource) drawVoltageSymbol(a, b);
        else if (e instanceof CurrentSource) drawCurrentSymbol(a, b);
        else if (e instanceof Diode)         drawDiodeSymbol(a, b);
    }

    private void drawResistorSymbol(Vector2 a, Vector2 b) {
        float dx = b.x - a.x, dy = b.y - a.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1f) return;
        float ux = dx / len, uy = dy / len;
        float px = -uy;

        float t0 = 0.25f, t1 = 0.75f, amp = 8f;
        int segs = 6;

        shapes.setColor(1f, 1f, 1f, 1f);
        float prevX = a.x + ux * len * t0;
        float prevY = a.y + uy * len * t0;
        for (int i = 1; i <= segs; i++) {
            float t = t0 + (t1 - t0) * i / segs;
            float sign = (i % 2 == 0) ? 0f : 1f;
            float x = a.x + ux * len * t + px * amp * sign;
            float y = a.y + uy * len * t + ux * amp * sign;
            shapes.rectLine(prevX, prevY, x, y, 2f);
            prevX = x; prevY = y;
        }
    }

    private void drawVoltageSymbol(Vector2 a, Vector2 b) {
        float dx = b.x - a.x, dy = b.y - a.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1f) return;
        float ux = dx / len, uy = dy / len;
        float px = -uy;
        float barLen = 12f;

        shapes.setColor(BG);
        shapes.rectLine(a.x + ux * len * 0.44f, a.y + uy * len * 0.44f,
                a.x + ux * len * 0.56f, a.y + uy * len * 0.56f, 4f);

        float cx1 = a.x + ux * len * 0.44f, cy1 = a.y + uy * len * 0.44f;
        shapes.setColor(1f, 0.85f, 0.3f, 1f);
        shapes.rectLine(cx1 - px * barLen, cy1 - ux * barLen,
                cx1 + px * barLen, cy1 + ux * barLen, 3f);

        float cx2 = a.x + ux * len * 0.56f, cy2 = a.y + uy * len * 0.56f;
        shapes.rectLine(cx2 - px * barLen * 0.6f, cy2 - ux * barLen * 0.6f,
                cx2 + px * barLen * 0.6f, cy2 + ux * barLen * 0.6f, 3f);
    }

    private void drawCurrentSymbol(Vector2 a, Vector2 b) {
        float dx = b.x - a.x, dy = b.y - a.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1f) return;
        float ux = dx / len, uy = dy / len;
        float px = -uy;
        float cx = (a.x + b.x) / 2f, cy = (a.y + b.y) / 2f;

        shapes.setColor(BG);
        shapes.circle(cx, cy, 14f, 24);
        shapes.setColor(0.35f, 0.35f, 0.4f, 1f);
        shapes.circle(cx, cy, 14f, 24);

        shapes.setColor(1f, 0.7f, 0.3f, 1f);
        shapes.rectLine(cx - ux * 8f, cy - uy * 8f, cx + ux * 8f, cy + uy * 8f, 2f);
        shapes.triangle(
                cx + ux * 10f, cy + uy * 10f,
                cx + ux * 5f + px * 5f, cy + uy * 5f + ux * 5f,
                cx + ux * 5f - px * 5f, cy + uy * 5f - ux * 5f);
    }

    private void drawDiodeSymbol(Vector2 a, Vector2 b) {
        float dx = b.x - a.x, dy = b.y - a.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1f) return;
        float ux = dx / len, uy = dy / len;
        float px = -uy;
        float cx = (a.x + b.x) / 2f, cy = (a.y + b.y) / 2f;

        shapes.setColor(BG);
        shapes.rectLine(a.x + ux * len * 0.35f, a.y + uy * len * 0.35f,
                a.x + ux * len * 0.65f, a.y + uy * len * 0.65f, 4f);

        shapes.setColor(0.3f, 0.9f, 0.4f, 1f);
        float apex = 8f, baseHalf = 8f;
        shapes.triangle(
                cx + ux * apex, cy + uy * apex,
                cx - ux * apex + px * baseHalf, cy - uy * apex + ux * baseHalf,
                cx - ux * apex - px * baseHalf, cy - uy * apex - ux * baseHalf);
        float bx = cx + ux * apex, by = cy + uy * apex;
        shapes.rectLine(bx + px * baseHalf, by + ux * baseHalf,
                bx - px * baseHalf, by - ux * baseHalf, 3f);
    }

    // ------------------------------------------------------------------
    // drag-линия
    // ------------------------------------------------------------------
    private void drawDrag() {
        Vector2 a = location.positionOf(location.getDragFrom());
        if (a == null) return;
        shapes.setColor(1f, 1f, 0f, 0.6f);
        shapes.rectLine(a.x, a.y, location.getDragX(), location.getDragY(), 2f);
    }

    // ------------------------------------------------------------------
    // подписи
    // ------------------------------------------------------------------
    private void drawLabels() {
        for (Point p : network.getPoints()) {
            Vector2 v = location.positionOf(p);
            if (v == null) continue;
            font.setColor(p.isFixed() ? new Color(0.5f, 1f, 0.5f, 1f) : Color.WHITE);
            font.draw(batch, String.format("%.2f V", p.getPotential()), v.x + 14, v.y + 26);
        }
        font.setColor(0.85f, 0.85f, 0.9f, 1f);
        for (Element e : network.getElements()) {
            Vector2 a = location.positionOf(e.getInput());
            Vector2 b = location.positionOf(e.getOutput());
            if (a == null || b == null) continue;
            font.draw(batch, e.getInfo().getName(),
                    (a.x + b.x) / 2f + 8f, (a.y + b.y) / 2f + 4f);
        }
    }

    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }
}
