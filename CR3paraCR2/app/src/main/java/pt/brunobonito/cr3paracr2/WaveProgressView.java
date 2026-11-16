package pt.brunobonito.cr3paracr2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class WaveProgressView extends View {
    private final Paint active = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stop = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float progress;
    private float phase;
    private final float stroke, amplitude, wavelength, gap;

    public WaveProgressView(Context c) { this(c, null); }
    public WaveProgressView(Context c, AttributeSet attrs) {
        super(c, attrs);
        stroke = dp(6); amplitude = dp(4); wavelength = dp(48); gap = dp(10);
        active.setStrokeWidth(stroke); active.setStrokeCap(Paint.Cap.ROUND); active.setStrokeJoin(Paint.Join.ROUND);
        track.setStrokeWidth(stroke); track.setStrokeCap(Paint.Cap.ROUND);
        stop.setStyle(Paint.Style.FILL);
    }

    public void setColors(int primary, int trackColor) {
        active.setColor(primary); stop.setColor(primary); track.setColor(trackColor); invalidate();
    }

    public void setProgress(float value) {
        progress = Math.max(0f, Math.min(1f, value));
        phase += .14f;
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float left = dp(8), right = getWidth() - dp(8), cy = getHeight() / 2f;
        if (right <= left) return;
        float stopRadius = dp(3);
        float activeEnd = left + (right - left) * progress;

        // Active portion: standalone Material 3 Expressive wave.
        if (activeEnd > left) {
            Path path = new Path();
            path.moveTo(left, cy);
            for (float x = left; x <= activeEnd; x += dp(.8f)) {
                float d = x - left;
                float toEnd = activeEnd - x;
                float envelope = Math.min(1f, Math.min(d / dp(14), Math.max(0f, toEnd / dp(14))));
                float y = (float) (cy + Math.sin((d / wavelength) * Math.PI * 2 + phase) * amplitude * envelope);
                path.lineTo(x, y);
            }
            canvas.drawPath(path, active);
        }

        // Inactive track begins after a visible gap. It is never drawn below the wave.
        float trackStart = activeEnd + gap;
        float trackEnd = right - stopRadius - dp(8);
        if (trackEnd > trackStart) canvas.drawLine(trackStart, cy, trackEnd, cy, track);
        canvas.drawCircle(right, cy, stopRadius, stop);
    }

    @Override protected void onMeasure(int w, int h) {
        setMeasuredDimension(resolveSize(getSuggestedMinimumWidth(), w), resolveSize((int) dp(40), h));
    }

    private float dp(float v) { return v * getResources().getDisplayMetrics().density; }
}
