package com.axelwiston.solarsystemexplorer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

public class PlanetView extends View {
    public interface OnPlanetSelectedListener {
        void onPlanetSelected(int index);
    }

    private OnPlanetSelectedListener listener;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float[] x = new float[9];
    private final float[] y = new float[9];
    private final float[] radius = new float[9];

    private final int[] colors = {
        0xFFFFC84A, 0xFFB7A18A, 0xFFE9B878, 0xFF3E91D5,
        0xFFE45B43, 0xFFD9A66B, 0xFFE8C778, 0xFF72D5D0,
        0xFF426FE0
    };

    private final String[] labels = {
        "Sun", "Mercury", "Venus", "Earth", "Mars",
        "Jupiter", "Saturn", "Uranus", "Neptune"
    };

    private final float density;

    public PlanetView(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setClickable(true);
    }

    public void setOnPlanetSelectedListener(OnPlanetSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final float width = getWidth();
        final float height = getHeight();
        if (width <= 0 || height <= 0) return;

        canvas.drawColor(0xFF101A43);

        // Scale the drawing to the available space. The original fixed-size
        // orbits could put planets outside narrow Samsung device screens.
        float scale = Math.min(1f, Math.min(
            (width / density) * 0.46f / 215f,
            (height / density) * 0.43f / 103f
        ));
        float unit = density * Math.max(0.35f, scale);

        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 65; i++) {
            float sx = ((i * 83) % 997) / 997f * width;
            float sy = ((i * 47) % 991) / 991f * height;
            paint.setColor(i % 5 == 0 ? 0xFFFFD98B : 0xFFB9C9FF);
            canvas.drawCircle(sx, sy, (i % 7 == 0 ? 2.1f : 1.1f) * density, paint);
        }

        float centerX = width / 2f;
        float centerY = height / 2f;
        float[] orbit = {0, 42, 65, 88, 111, 139, 166, 191, 215};

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1f, density));
        paint.setColor(0x557C91D8);
        for (int i = 1; i < 9; i++) {
            float distance = orbit[i] * unit;
            canvas.drawOval(new RectF(
                centerX - distance, centerY - distance * 0.48f,
                centerX + distance, centerY + distance * 0.48f
            ), paint);
        }

        x[0] = centerX;
        y[0] = centerY;
        radius[0] = 20f * unit;

        for (int i = 1; i < 9; i++) {
            x[i] = centerX - orbit[i] * 0.82f * unit;
            y[i] = centerY - orbit[i] * 0.39f * unit;
            float size = i == 5 ? 13f : (i == 6 ? 12f :
                (i == 1 ? 5f : (i == 2 || i == 3 || i == 4 ? 8f : 9f)));
            radius[i] = size * unit;
        }

        for (int i = 0; i < 9; i++) {
            float r = radius[i];
            int light = lighten(colors[i]);
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(new RadialGradient(
                x[i] - r * 0.35f, y[i] - r * 0.4f, Math.max(r * 1.5f, 1f),
                new int[]{light, colors[i], 0xFF171B35},
                null, Shader.TileMode.CLAMP
            ));
            canvas.drawCircle(x[i], y[i], r, paint);
            paint.setShader(null);

            if (i == 6) {
                paint.setColor(0xFFDCC48A);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(Math.max(2f, 3.2f * unit));
                canvas.drawOval(new RectF(
                    x[i] - r * 1.7f, y[i] - r * 0.45f,
                    x[i] + r * 1.7f, y[i] + r * 0.45f
                ), paint);
            }

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFFEAF0FF);
            paint.setTextSize(10f * unit);
            canvas.drawText(labels[i], x[i] - paint.measureText(labels[i]) / 2f,
                y[i] + r + 13f * unit, paint);
        }

        paint.setColor(0xFFFFD56A);
        paint.setTextSize(10f * density);
        paint.setStyle(Paint.Style.FILL);
        String hint = "TAP A PLANET";
        canvas.drawText(hint, centerX - paint.measureText(hint) / 2f,
            height - 8f * density, paint);
    }

    private int lighten(int color) {
        return Color.rgb(
            Math.min(255, Color.red(color) + 65),
            Math.min(255, Color.green(color) + 65),
            Math.min(255, Color.blue(color) + 65)
        );
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int selected = -1;
            float closest = Float.MAX_VALUE;

            for (int i = 0; i < 9; i++) {
                float dx = event.getX() - x[i];
                float dy = event.getY() - y[i];
                float distanceSquared = dx * dx + dy * dy;
                float hitRadius = radius[i] + 18f * density;
                if (distanceSquared <= hitRadius * hitRadius &&
                    distanceSquared < closest) {
                    selected = i;
                    closest = distanceSquared;
                }
            }

            if (selected >= 0 && listener != null) {
                listener.onPlanetSelected(selected);
            }
        }
        return true;
    }
}
