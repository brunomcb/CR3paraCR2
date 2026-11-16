package pt.brunobonito.cr3paracr2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class WaveProgressView extends View {
    private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint active = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stop = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float progress = 0f;
    private float phase = 0f;
    private final float trackThickness;
    private final float activeThickness;
    private final float amplitude;
    private final float wavelength;

    public WaveProgressView(Context c) { this(c, null); }
    public WaveProgressView(Context c, AttributeSet a) {
        super(c, a);
        trackThickness = dp(4);
        activeThickness = dp(4);
        amplitude = dp(3);
        wavelength = dp(24);
        track.setStrokeWidth(trackThickness);
        track.setStrokeCap(Paint.Cap.ROUND);
        active.setStrokeWidth(activeThickness);
        active.setStrokeCap(Paint.Cap.ROUND);
        stop.setStyle(Paint.Style.FILL);
        setMinimumHeight((int) dp(28));
    }

    public void setColors(int primary, int secondaryContainer) {
        active.setColor(primary);
        stop.setColor(primary);
        track.setColor(secondaryContainer);
        invalidate();
    }

    public void setProgress(float value) {
        progress = Math.max(0f, Math.min(1f, value));
        phase += 0.22f;
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float inset = dp(4) + Math.max(activeThickness / 2f, amplitude);
        float left = inset;
        float right = getWidth() - inset;
        float centerY = getHeight() / 2f;
        if (right <= left) return;

        canvas.drawLine(left, centerY, right, centerY, track);
        float end = left + (right - left) * progress;

        if (end > left) {
            Path wave = new Path();
            wave.moveTo(left, centerY);
            for (float x = left; x <= end; x += dp(0.75f)) {
                float distanceFromStart = x - left;
                float distanceToEnd = end - x;
                float startRamp = Math.min(1f, distanceFromStart / (wavelength / 2f));
                float endRamp = Math.min(1f, Math.max(0f, distanceToEnd / (wavelength / 2f)));
                float envelope = Math.min(startRamp, endRamp);
                float y = (float) (centerY + Math.sin((distanceFromStart / wavelength) * Math.PI * 2 + phase) * amplitude * envelope);
                wave.lineTo(x, y);
            }
            canvas.drawPath(wave, active);
        }

        float stopRadius = dp(2);
        canvas.drawCircle(right, centerY, stopRadius, stop);
    }

    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        int desiredHeight = (int) dp(28);
        int width = resolveSize(getSuggestedMinimumWidth(), widthSpec);
        int height = resolveSize(desiredHeight, heightSpec);
        setMeasuredDimension(width, height);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
