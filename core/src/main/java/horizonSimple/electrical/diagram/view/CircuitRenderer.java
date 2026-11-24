package horizonSimple.electrical.diagram.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import horizonSimple.electrical.ElectricalNetwork;
import horizonSimple.electrical.Element;
import horizonSimple.electrical.Point;
import horizonSimple.electrical.diagram.DiagramContext;

public class CircuitRenderer {
    private static final float POINT_R = 10f;
    public static final Color BG = new Color(0.10f, 0.11f, 0.13f, 1f);

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();

    private final Matrix4 transform;

    private final OrthographicCamera projectionCamera;
    private final ElectricalNetwork network;
    private final ElementsLocation location;
    private final SelectionState selection;

    public CircuitRenderer(DiagramContext context) {
        this.projectionCamera = context.camera;
        this.network = context.network;
        this.location = context.location;
        this.selection = context.selection;
        font.getData().setScale(1.0f);

        transform = new Matrix4();
    }

    public void draw() {
        Gdx.gl.glClearColor(BG.r, BG.g, BG.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        projectionCamera.update();

        shapes.setProjectionMatrix(projectionCamera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        for (Element e : network.getElements()) drawConnect(e);
        for (Point p : network.getPoints())     drawPoint(p);
        if (location.isDragging())              drawDrag();

        shapes.end();

        batch.setProjectionMatrix(projectionCamera.projection);
        batch.begin();

        for (Element e : network.getElements()) drawElement(e);
        drawLabels();
        batch.end();
    }

    // ------------------------------------------------------------------
    // точки
    // ------------------------------------------------------------------
    private void drawPoint(Point p) {
        Vector2 v = location.positionOf(p);
        if (v == null) return;
        double t = Math.max(-1, Math.min(1, p.getPotential() / 10));

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

    private static final float ELEMENT_H  = 32f;
    private static final float ICON_MAX_H = 40f;      // предел высоты спрайта

    private final Matrix4 identity = new Matrix4();   // переиспользуем

    private void drawElement(Element e) {
        // (1) null-safe
        if (e.getInfo() == null) return;
        Drawable drawable = e.getInfo().getDrawable();
        if (drawable == null) return;

        Vector2 a = location.positionOf(e.getInput());
        Vector2 b = location.positionOf(e.getOutput());
        if (a == null || b == null) return;

        float dx = b.x - a.x, dy = b.y - a.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1f) return;

        float cx = (a.x + b.x) / 2f;
        float cy = (a.y + b.y) / 2f;
        float angle = (float) Math.toDegrees(Math.atan2(dy, dx));

        // (2) сохраняем пропорции иконки и не растягиваем выше ICON_MAX_H
        if (drawable instanceof TextureRegionDrawable) {
            TextureRegion region = ((TextureRegionDrawable)drawable).getRegion();
            float rw = region.getRegionWidth();
            float rh = region.getRegionHeight();
            if (rw < 1f || rh < 1f) return;

            // спрайт растягивается вдоль провода на len,
            // поперёк — на столько, чтобы сохранить пропорцию,
            // но не выше ICON_MAX_H
            float drawW = len;
            float drawH = drawW * rh / rw;
            if (drawH > ICON_MAX_H) {
                drawH = ICON_MAX_H;
                drawW = drawH * rw / rh;
            }
            batch.setColor(Color.WHITE);
            batch.setTransformMatrix(identity);
            batch.setProjectionMatrix(projectionCamera.combined);

            batch.draw(region,
                cx - drawW / 2f, cy - drawH / 2f,
                drawW / 2f,       drawH / 2f,
                drawW,            drawH,
                1f, 1f,
                angle);
            return;
        }

        // (3) fallback — через отдельный transform
        transform.set(projectionCamera.view);
        transform.translate(cx, cy, 0);
        transform.rotate(0, 0, 1, angle);

        batch.setProjectionMatrix(projectionCamera.projection);
        batch.setTransformMatrix(transform);
        drawable.draw(batch, -len / 2f, -ELEMENT_H / 2f, len, ELEMENT_H);

        batch.setTransformMatrix(identity);
        batch.setProjectionMatrix(projectionCamera.combined);
    }

    private void drawConnect(Element element) {
        Vector2 a = location.positionOf(element.getInput());
        Vector2 b = location.positionOf(element.getOutput());
        if (a == null || b == null) return;

        shapes.setColor(Color.WHITE);
        shapes.line(a, b);
    }

    private void drawResistorSymbol(Vector2 a, Vector2 b) {
        float dx = b.x - a.x, dy = b.y - a.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1f) return;
        float ux = dx / len, uy = dy / len;
        float px = -uy, py = ux;

        float t0 = 0.25f, t1 = 0.75f, amp = 8f;
        int segs = 6;

        shapes.setColor(1f, 1f, 1f, 1f);
        float prevX = a.x + ux * len * t0;
        float prevY = a.y + uy * len * t0;
        for (int i = 1; i <= segs; i++) {
            float t = t0 + (t1 - t0) * i / segs;
            float sign = (i % 2 == 0) ? 0f : 1f;
            float x = a.x + ux * len * t + px * amp * sign;
            float y = a.y + uy * len * t + py * amp * sign;
            shapes.rectLine(prevX, prevY, x, y, 2f);
            prevX = x; prevY = y;
        }
    }

    private void drawVoltageSymbol(Vector2 a, Vector2 b) {
        float dx = b.x - a.x, dy = b.y - a.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1f) return;
        float ux = dx / len, uy = dy / len;
        float px = -uy, py = ux;
        float barLen = 12f;

        shapes.setColor(BG);
        shapes.rectLine(a.x + ux * len * 0.44f, a.y + uy * len * 0.44f,
            a.x + ux * len * 0.56f, a.y + uy * len * 0.56f, 4f);

        float cx1 = a.x + ux * len * 0.44f, cy1 = a.y + uy * len * 0.44f;
        shapes.setColor(1f, 0.85f, 0.3f, 1f);
        shapes.rectLine(cx1 - px * barLen, cy1 - py * barLen,
            cx1 + px * barLen, cy1 + py * barLen, 3f);

        float cx2 = a.x + ux * len * 0.56f, cy2 = a.y + uy * len * 0.56f;
        shapes.rectLine(cx2 - px * barLen * 0.6f, cy2 - py * barLen * 0.6f,
            cx2 + px * barLen * 0.6f, cy2 + py * barLen * 0.6f, 3f);
    }

    private void drawCurrentSymbol(Vector2 a, Vector2 b) {
        float dx = b.x - a.x, dy = b.y - a.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1f) return;
        float ux = dx / len, uy = dy / len;
        float px = -uy, py = ux;
        float cx = (a.x + b.x) / 2f, cy = (a.y + b.y) / 2f;

        shapes.setColor(BG);
        shapes.circle(cx, cy, 14f, 24);
        shapes.setColor(0.35f, 0.35f, 0.4f, 1f);
        shapes.circle(cx, cy, 14f, 24);

        shapes.setColor(1f, 0.7f, 0.3f, 1f);
        shapes.rectLine(cx - ux * 8f, cy - uy * 8f, cx + ux * 8f, cy + uy * 8f, 2f);
        shapes.triangle(
            cx + ux * 10f, cy + uy * 10f,
            cx + ux * 5f + px * 5f, cy + uy * 5f + py * 5f,
            cx + ux * 5f - px * 5f, cy + uy * 5f - py * 5f);
    }

    private void drawDiodeSymbol(Vector2 a, Vector2 b) {
        float dx = b.x - a.x, dy = b.y - a.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1f) return;
        float ux = dx / len, uy = dy / len;
        float px = -uy, py = ux;
        float cx = (a.x + b.x) / 2f, cy = (a.y + b.y) / 2f;

        shapes.setColor(BG);
        shapes.rectLine(a.x + ux * len * 0.35f, a.y + uy * len * 0.35f,
            a.x + ux * len * 0.65f, a.y + uy * len * 0.65f, 4f);

        shapes.setColor(0.3f, 0.9f, 0.4f, 1f);
        float apex = 8f, baseHalf = 8f;
        shapes.triangle(
            cx + ux * apex, cy + uy * apex,
            cx - ux * apex + px * baseHalf, cy - uy * apex + py * baseHalf,
            cx - ux * apex - px * baseHalf, cy - uy * apex - py * baseHalf);
        float bx = cx + ux * apex, by = cy + uy * apex;
        shapes.rectLine(bx + px * baseHalf, by + py * baseHalf,
            bx - px * baseHalf, by - py * baseHalf, 3f);
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
    }

    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }
}
